package com.zappy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.zappy.Zappy;
import com.zappy.assets.Assets;
import com.zappy.network.Network;
import com.zappy.popup.ErrorPopUp;

import java.io.IOException;

/**
 * Created by debas_e on 24/06/2014.
 */
public class MainMenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private TextField ipTextField, portTextField;
    private TextButton connectionButton;
    private Group background;
    private String ipString = "", portString = "";
    private Batch batch;

    private int CONNECT_BUTTON_ID = 2;
    private Zappy game;

    public static int WIDTH, HEIGHT;

    public MainMenuScreen(Zappy game) {
        this.game = game;
        create();
    }

    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        stage = new Stage();
        skin = new Skin(Gdx.files.internal("data/mainMenu/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.setSize(WIDTH, HEIGHT);

        ipTextField = new TextField("lodow.net", skin);
        portTextField = new TextField("4242", skin);

        Label ipLabel = new Label("Ip   : ", skin);
        Label portLabel = new Label("port : ", skin);

        connectionButton = new TextButton("Connect", skin);
        connectionButton.setDisabled(true);
        connectionButton.addListener(new ConnectListener(CONNECT_BUTTON_ID));

        background = new Group();
        background.setBounds(0, 0, WIDTH, HEIGHT);
        background.addActor(new Image(new Texture(Gdx.files.internal("data/mainMenu/zappy_main.png"))));

        Texture cross = new Texture(Gdx.files.internal("data/mainMenu/remove_cross.png"));

        Image cross_ip = new Image(cross);
        cross_ip.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                ipTextField.setText("");
                return true;
            }
        });

        Image cross_port = new Image(cross);
        cross_port.addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                portTextField.setText("");
                return true;
            }
        });

        table.add(ipLabel).padBottom(20);
        table.add(ipTextField).width(WIDTH / 4).padBottom(20).padRight(20);
        table.add(cross_ip).padBottom(20);
        table.row();
        table.add(portLabel).padBottom(20);
        table.add(portTextField).width(WIDTH / 4).padBottom(40).padRight(20);
        table.add(cross_port).padBottom(30);
        table.row();
        table.add(connectionButton).colspan(5).width(WIDTH / 4);
        table.left().bottom().padBottom(150);

        stage.addActor(background);
        stage.addActor(table);

        batch = new SpriteBatch();

        Gdx.input.setInputProcessor(stage);

        Assets.menuMusic.play();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(1, 1, 1, 1);

        if (ipTextField.getText().length() > 0 && portTextField.getText().length() > 0) {
            connectionButton.setDisabled(false);
        } else if (!connectionButton.isDisabled()) {
            connectionButton.setDisabled(true);
        }

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        Assets.soundController.draw(batch, this);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    class ConnectListener extends ClickListener {

        private int mButton = -1;
        private int port = 0;

        ConnectListener(int button) {
            mButton = button;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (mButton == CONNECT_BUTTON_ID && !connectionButton.isDisabled()) {
                boolean error = false;
                ipString = ipTextField.getText();
                portString = portTextField.getText();
                String errorMsg = "";

                try {
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    error = true;
                }

                if (error || port < 0 || port > 65535) {
                    ErrorPopUp err = new ErrorPopUp("", "Invalid Port !\nPlease give a valid one",
                            new TextButton("Retry", skin), skin);
                    err.show(stage);
                } else {
                        Network network = null;
                        try {
                            network = new Network(ipString, port);
                            Assets.menuMusic.stop();
                            Assets.gameMusic.play();
                            game.setScreen(new MapViewer(network, game, skin));
                        } catch (IOException e) {
                            Gdx.app.log("network", "error network" + ipString + " - " + port);
                            errorMsg = "error network" + ipString + " - " + port;
                        } catch (NumberFormatException e) {
                            Gdx.app.log("network", "error network" + ipString + " - " + port);
                            errorMsg = "error network" + ipString + " - " + port;
                        }
                        if (errorMsg.length() > 0) {
                            ErrorPopUp err = new ErrorPopUp("", "Error establishing connection with\nIp : " + ipString + "\n" + "Port : " + port,
                                    new TextButton("Retry", skin), skin);
                            err.show(stage);
                        }
                    }
                }
            }
        }
    }
