package com.opethic.genivis.controller.dialogs;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.controller.commons.OverlaysEffect;
import com.opethic.genivis.controller.commons.SwitchButton;
import com.opethic.genivis.controller.master.ledger.common.LedgerCommonController;
import com.opethic.genivis.controller.tranx_sales.SalesQuotationCreateController;
import com.opethic.genivis.controller.tranx_sales.common.TextFieldTableCellForSalesInvoiceWithConverter;
import com.opethic.genivis.controller.tranx_sales.common.TranxCommonPopUps;
import com.opethic.genivis.dto.*;
import com.opethic.genivis.dto.counter.CounterSaleAddPrescritDTO;
import com.opethic.genivis.dto.pur_invoice.reqres.PurchaseReturnBillListDTO;
import com.opethic.genivis.dto.pur_invoice.reqres.PurchaseReturnBillListDTO;
import com.opethic.genivis.dto.reqres.BillByBillRes;
import com.opethic.genivis.dto.reqres.creditNote.BillByBillPerticulars;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.dto.reqres.pur_tranx.*;
import com.opethic.genivis.models.AccountEntry.ReceiptPaymentModeDTO;
import com.opethic.genivis.models.AccountEntry.TranxReceiptWindowDTO;
import com.opethic.genivis.models.master.ledger.CommonOption;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.logging.log4j.core.util.ArrayUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.Position;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.opethic.genivis.network.EndPoints.PATIENT_LIST_ENDPOINT;
import static com.opethic.genivis.utils.FxmFileConstants.*;

public class SingleInputDialogs {
    public static String productIdSelected = "", invoiceDate = "";

    public static String ledId = "", productId = "";
    public static int index1 = 0, index2 = 0;
    private static String inputName = "";
    private static String hsnType = "";

    private static ObservableList<CommonDTO> contentNamesList = FXCollections.observableArrayList();
    private static ObservableList<CommonDTO> contentNamesList1 = FXCollections.observableArrayList();
    private static ObservableList<CommonDTO> contentNamesList2 = FXCollections.observableArrayList();
    private static ObservableList<CommonDTO> contentNamesList3 = FXCollections.observableArrayList();
    private static ObservableList<CounterSaleAddPrescritDTO> observableListCounterSPriscri = FXCollections.observableArrayList();
    private static ObservableList<TranxLedgerWindowDTO> observableListLedger = FXCollections.observableArrayList();
    private static ObservableList<PatientDTO> observableListPatient = FXCollections.observableArrayList();
    private static ObservableList<DoctorMasterDTO> observableListDoctor = FXCollections.observableArrayList();
    private static ObservableList<TranxReturnsBillDTO> observableLedgerBillList = FXCollections.observableArrayList();
    private static ObservableList<TranxBatchWindowDTO> observableListBatch = FXCollections.observableArrayList();
    private static ObservableList<TranxProductWindowDTO> observableListProduct = FXCollections.observableArrayList();
    private static ObservableList<PurchaseReturnBillListDTO> observableListPurchaseReturnList = FXCollections.observableArrayList();
    private static ObservableList<TranxReceiptWindowDTO> observableBillByBill = FXCollections.observableArrayList();
    private static ObservableList<TranxRtnsBillProductListDTO> observableTranxRtnProductList =
            FXCollections.observableArrayList();
    private static ObservableList<TranxPurRowResEditDTO> productRowList =
            FXCollections.observableArrayList();
    private static TranxPurInvoiceResEditDTO invoiceResEditDTO = null;
    private static TranxSalesInvoiceResEditDTO invoiceResEditDTOSalesRtn = null;
    private static final Logger logger = LoggerFactory.getLogger(SingleInputDialogs.class);
    private static String payable_Amt;
    private static double label;
    private static TableView<TranxLedgerWindowDTO> tblLedger;
    private int contentEdtIdx = -1;

    private static Map<String, String> productMap = new HashMap<>();
    private static String message = "";

    /*** used for Create and Edit both scenarios, Send empty String While used in creation ****/
    public static void singleInputDialog(String input, Stage stage, String title, Consumer<String> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");

        //BorderPan under Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(498);
        hbox_top.setMaxWidth(498);
        hbox_top.setPrefWidth(498);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #d9f0fb; -fx-border-color: #ffffff; -fx-border-width: 0 0 2 0;");

        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_center = new HBox();
        hbox_center.setMinWidth(498);
        hbox_center.setMaxWidth(498);
        hbox_center.setPrefWidth(498);
        hbox_center.setAlignment(Pos.CENTER);
        hbox_center.setStyle("-fx-background-color: #e6f2f8;");
        TextField textField = new TextField();
        textField.setText(input);

        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                Platform.runLater(() -> {
                    textField.positionCaret(textField.getText().length());
                });
            }
        });


        String sub_title = "";
        if (title.startsWith("Add"))
            sub_title = title.substring(4);
        else {
            sub_title = title.substring(7);
        }
        textField.setPromptText(sub_title);

        textField.setMaxWidth(400);
        textField.setMaxWidth(400);
        textField.setPrefWidth(400);
        hbox_center.getChildren().addAll(textField);


        //BorderPane Under Bottom Layout..............................................................................................................
        HBox hbox_bottom = new HBox();
        hbox_bottom.setMinWidth(498);
        hbox_bottom.setMaxWidth(498);
        hbox_bottom.setPrefWidth(498);
        hbox_bottom.setMaxHeight(55);
        hbox_bottom.setMinHeight(55);
        hbox_bottom.setPrefHeight(55);
        hbox_bottom.setSpacing(10);
        hbox_bottom.setStyle("-fx-background-radius: 0 0 5 5; -fx-background-color: white;");
        hbox_bottom.setAlignment(Pos.CENTER_RIGHT);

//        Button cancelButton = new Button("Cancel");
        Button submitButton = null;
        if (title.startsWith("Add")) submitButton = new Button("Submit");
        else submitButton = new Button("Update");
        hbox_bottom.setMargin(submitButton, new Insets(0, 10, 0, 0));
        submitButton.setId("sub");
//        cancelButton.setId("cancel-btn");
//        cancelButton.setOnAction(event -> primaryStage.close());

        hbox_bottom.getChildren().addAll(submitButton);

        borderPane.setTop(hbox_top);
        borderPane.setCenter(hbox_center);
        borderPane.setBottom(hbox_bottom);

        Node[] nodes = new Node[]{textField, submitButton};
        CommonValidationsUtils.setupFocusNavigation(nodes);

        Scene scene = new Scene(borderPane, 500, 170);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (textField.getText().isEmpty()) {
                    textField.requestFocus();
                } else {
                    callback.accept(textField.getText());
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }

            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    public static void singleInputDialogForImage(List<File> imageFiles, Stage stage, String title, Consumer<String> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        // Main Layout........................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");

        // Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(798);
        hbox_top.setMaxWidth(798);
        hbox_top.setPrefWidth(798);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image closeImage = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView closeImageView = new ImageView(closeImage);
        closeImageView.setStyle("-fx-cursor: hand;");
        closeImageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(closeImageView, new Insets(0, 10, 0, 0));
        closeImageView.setFitWidth(30);
        closeImageView.setFitHeight(30);

        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(closeImageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #d9f0fb; -fx-border-color: #ffffff; -fx-border-width: 0 0 2 0;");

        // Center Layout.....................................................................................................
        FlowPane flowPane = new FlowPane();
        flowPane.setMinWidth(798);
        flowPane.setMaxWidth(798);
        flowPane.setPrefWidth(798);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setStyle("-fx-background-color: white;");
        flowPane.setHgap(10);
        flowPane.setVgap(10);

        // Load and display each image
        for (File imageFile : imageFiles) {
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(400); // Adjust width to fit better within the layout
            imageView.setFitHeight(175); // Adjust width to fit better within the layout
            imageView.setPreserveRatio(true);
            flowPane.getChildren().add(imageView);
        }

        borderPane.setTop(hbox_top);
        borderPane.setCenter(flowPane);

        Scene scene = new Scene(borderPane, 800, 600);  // Adjust the height to better fit the images

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();
        primaryStage.show();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }


    public static <T> void twoInputDialog(String input, Stage stage, String title, ObservableList<T> list,
                                          String key, BiConsumer<String, Object> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");

        //BorderPan under Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(698);
        hbox_top.setMaxWidth(698);
        hbox_top.setPrefWidth(698);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #d9f0fb; -fx-border-color: #ffffff; -fx-border-width: 0 0 2 0;");

        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_center = new HBox();
        hbox_center.setMinWidth(698);
        hbox_center.setMaxWidth(698);
        hbox_center.setPrefWidth(698);
        hbox_center.setAlignment(Pos.CENTER);
        hbox_center.setSpacing(16);
        hbox_center.setStyle("-fx-background-color: #e6f2f8;");

        TextField textField = new TextField();
        textField.setText(input);
        textField.setMaxWidth(300);
        textField.setMaxWidth(300);
        textField.setPrefWidth(300);
        textField.setPromptText("Unit Name");
        ComboBox comboBox = new ComboBox();
        comboBox.setItems(list);
        if (title.startsWith("Update")) {
            String selecteditem = "";
            for (Object obj : comboBox.getItems()) {
                if (obj != null) {
                    if (obj.toString().equals(key)) {
                        selecteditem = key;
                        break;
                    }
                }
            }
            if (selecteditem != null) {
                comboBox.getSelectionModel().select(selecteditem);
            }
        }
        comboBox.setPromptText("Unit Code");
        Integer initialIndex = -1;
        AutoCompleteBox autoCompleteBox = new AutoCompleteBox(comboBox, initialIndex);

        final Object[] selectedItem = {null};
        comboBox.setOnAction(event -> {
            selectedItem[0] = comboBox.getSelectionModel().getSelectedItem();

        });

        hbox_center.getChildren().addAll(textField, comboBox);


        //BorderPane Under Bottom Layout..............................................................................................................
        HBox hbox_bottom = new HBox();
        hbox_bottom.setMinWidth(698);
        hbox_bottom.setMaxWidth(698);
        hbox_bottom.setPrefWidth(698);
        hbox_bottom.setMaxHeight(55);
        hbox_bottom.setMinHeight(55);
        hbox_bottom.setPrefHeight(55);
        hbox_bottom.setSpacing(10);
        hbox_bottom.setStyle("-fx-background-radius: 0 0 5 5; -fx-background-color: white;");
        hbox_bottom.setAlignment(Pos.CENTER_RIGHT);
        Button submitButton = null;
        if (title.startsWith("Add"))
            submitButton = new Button("Submit");
        else
            submitButton = new Button("Update");
        hbox_bottom.setMargin(submitButton, new Insets(0, 10, 0, 0));
        submitButton.setId("submit-btn");


        hbox_bottom.getChildren().addAll(submitButton);

        borderPane.setTop(hbox_top);
        borderPane.setCenter(hbox_center);
        borderPane.setBottom(hbox_bottom);

        Node[] nodes = new Node[]{textField, comboBox, submitButton};
        CommonValidationsUtils.setupFocusNavigation(nodes);

        Scene scene = new Scene(borderPane, 700, 170);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (textField.getText().isEmpty()) {
                    textField.requestFocus();
                } else if (selectedItem[0] == null || selectedItem[0].toString().isEmpty()) {
                    comboBox.requestFocus();
                } else {
                    callback.accept(textField.getText(), selectedItem[0]);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            }
        });

    }


    public static void productHsnInputDialog(String hsnNo, Stage stage, String title, Consumer<String[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_hsn.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; " + "-fx-border-radius: 5; -fx-border-width: 0.8;");

        //BorderPan under Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(798);
        hbox_top.setMaxWidth(798);
        hbox_top.setPrefWidth(798);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #d9f0fb; -fx-border-color: #ffffff; -fx-border-width: 0 0 2 0;");

        HBox hbox_center = new HBox();
        hbox_center.setMinWidth(798);
        hbox_center.setMaxWidth(798);
        hbox_center.setPrefWidth(798);
        hbox_center.setAlignment(Pos.CENTER_LEFT);
        hbox_center.setStyle("-fx-background-color: #e6f2f8;");
        TextField textField = new TextField("");
        textField.setPromptText("Hsn No");
        textField.setPadding(new Insets(10, 10, 0, 10));
        textField.setPrefWidth(200);

        textField.setText(hsnNo);
        Platform.runLater(() -> {
            Platform.runLater(() -> textField.positionCaret(textField.getText().length()));
        });

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (textField.getText().isEmpty()) {
                    textField.requestFocus();
                }
            }
        });


        HBox.setMargin(textField, new Insets(0, 0, 0, 20));
        TextField description = new TextField("");
        description.setPromptText("Description");
        description.setPadding(new Insets(10, 10, 0, 0));
        description.setPrefWidth(350);
        String type[] = {"Services", "Goods"};
        ComboBox cmbtype = new ComboBox(FXCollections.observableArrayList(type));
        cmbtype.setPromptText("type");
        cmbtype.setPrefWidth(200);
        cmbtype.setMinHeight(32);
        cmbtype.setMaxHeight(32);
        // Set default value
        cmbtype.setValue("Goods");
        // Create action event

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                hsnType = (String) cmbtype.getValue();
            }
        };
        // Set on action
        cmbtype.setOnAction(event);


        cmbtype.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String s = String.valueOf(cmbtype.getValue());

                if (s.equals("null")) {
                    cmbtype.requestFocus();
                }
            } else {
                cmbtype.show();
            }
        });
        HBox.setMargin(cmbtype, new Insets(0, 20, 0, 0));
        hbox_center.getChildren().addAll(textField, description, cmbtype);
        hbox_center.setSpacing(15);
        //BorderPane Under Bottom Layout..............................................................................................................
        HBox hbox_bottom = new HBox();
        hbox_bottom.setMinWidth(798);
        hbox_bottom.setMaxWidth(798);
        hbox_bottom.setPrefWidth(798);
        hbox_bottom.setMaxHeight(55);
        hbox_bottom.setMinHeight(55);
        hbox_bottom.setPrefHeight(55);
        hbox_bottom.setSpacing(10);
        hbox_bottom.setStyle("-fx-background-radius: 0 0 5 5; -fx-background-color: white;");
        hbox_bottom.setAlignment(Pos.CENTER_RIGHT);

//        Button cancelButton = new Button("Cancel");
        Button submitButton = new Button("Submit");
        hbox_bottom.setMargin(submitButton, new Insets(0, 10, 0, 0));
        submitButton.setId("submit-btn");
//        cancelButton.setId("cancel-btn");
//        cancelButton.setOnAction(event -> primaryStage.close());

        hbox_bottom.getChildren().addAll(submitButton);

        borderPane.setTop(hbox_top);
        borderPane.setCenter(hbox_center);
        borderPane.setBottom(hbox_bottom);

        Scene scene = new Scene(borderPane, 800, 170);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String input[] = {textField.getText(), description.getText(), hsnType};
                callback.accept(input);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        scene.setOnKeyPressed(events -> {
            if (events.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent eve) -> {
            if (eve.getCode() == KeyCode.ENTER) {
                if (eve.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("submit")) {
                    System.out.println(targetButton.getText());
                } else {
                    KeyEvent newEvent = new KeyEvent(
                            null,
                            null,
                            KeyEvent.KEY_PRESSED,
                            "",
                            "\t",
                            KeyCode.TAB,
                            eve.isShiftDown(),
                            eve.isControlDown(),
                            eve.isAltDown(),
                            eve.isMetaDown()
                    );
                    Event.fireEvent(eve.getTarget(), newEvent);
                    eve.consume();
                }
            }
        });

    }

    public static <T> void openLedgerPopUpForPurchaseInvoice(Stage stage, String ledger_id, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
//        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label("Filter");
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();

        VBox vboxGvProduct = new VBox();
        vboxGvProduct.setPrefHeight(23);
        vboxGvProduct.setMaxHeight(23);
        vboxGvProduct.setMinHeight(23);
        vboxGvProduct.setPrefWidth(35.5);
        vboxGvProduct.setMinWidth(35.5);
        vboxGvProduct.setMaxWidth(35.5);
        vboxGvProduct.setStyle("-fx-background-color: #f8f4fc; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #f8f4fc; -fx-border-width: 2;");
        vboxGvProduct.getChildren().add(switchButton);

        switchButton.setParentBox(vboxGvProduct);

        Label titleLabel = new Label(title);
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(vboxGvProduct);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Ledger");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);


        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[11];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(430);
        tableView.setMaxHeight(430);
        tableView.setMinHeight(430);

        tblLedger = tableView;

        //focus to search field by default in Ledger Popup
        Platform.runLater(() -> {
            if (ledId.equalsIgnoreCase("")) {
                search.requestFocus();
            }

        });





       /* search.setOnKeyPressed(event -> {
            System.out.println("Search Key--->" + search.getText());
            fetchDataOfAllTransactionLedgers(search.getText(), "");
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN)
                tableView.getSelectionModel().selectFirst();
            tableView.requestFocus();
            event.consume();
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                tableView.setItems(observableListLedger);
            }
        });

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                addButton.requestFocus();
            }
        });

        addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                search.requestFocus();
            }
        });


        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        //   fetchDataOfAllTransactionLedgers("", "");
        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);

        switchButton.switchOnProperty().set(true);

        if (switchButton.switchOnProperty().get()) {
            if (title.equalsIgnoreCase("debtors")) {
                fetchDataOfAllTransactionLedgers("", "SD");

            } else if (title.equalsIgnoreCase("creditors")) {
                fetchDataOfAllTransactionLedgers("", "SC");

            } else {
                fetchDataOfAllTransactionLedgers("", "");

            }
        }


        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                if (title.equalsIgnoreCase("debtors"))
                    fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                else if (title.equalsIgnoreCase("creditors")) {
                    fetchDataOfAllTransactionLedgers("", "SC"); // Fetch only SD type ledgers
                } else fetchDataOfAllTransactionLedgers("", "");

            } else {
                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
                System.out.println("test 2");
            }
        });
        tableView.setItems(observableListLedger);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
       /* search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });*/

        List<TranxLedgerWindowDTO> list = tableView.getItems();

        int index = -1;
        int count = 0;
        for (TranxLedgerWindowDTO tranxLedgerWindowDTO : list) {
            if (tranxLedgerWindowDTO.getId().equals(ledger_id)) {
                index = count;
            }
            count++;
        }

        int finalIndex = index;
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(finalIndex);
            tableView.scrollTo(finalIndex);
        });


        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();

                        System.out.println(ledgerWindowDTO.getSalesmanId());
//                        ObservableList<SalesmanMasterDTO> salesManDetails = ledgerWindowDTO.getSalesmanId();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getStateCode();
                        selectedItem[4] = ledgerWindowDTO.getType();
                        selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                        selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                        selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                        selectedItem[9] = ledgerWindowDTO.getPendingChallans();
                        callback.accept(selectedItem);
                        gstDetails.clear();
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            row.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {

                    TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                    System.out.println("Ledger Selection-->" + ledgerWindowDTO.getStateCode());
                    ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                    selectedItem[0] = ledgerWindowDTO.getLedger_name();
                    selectedItem[1] = ledgerWindowDTO.getId();
                    selectedItem[2] = gstDetails;
                    selectedItem[3] = ledgerWindowDTO.getStateCode();
                    selectedItem[4] = ledgerWindowDTO.getType();
                    selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                    selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                    selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                    selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                    selectedItem[9] = ledgerWindowDTO.getPendingChallans();

                    callback.accept(selectedItem);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            });
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println(".0" +
                        "" +
                        " Selection-->");
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getStateCode();
                selectedItem[4] = ledgerWindowDTO.getType();
                selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                selectedItem[9] = ledgerWindowDTO.getPendingChallans();
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GlobalController.getInstance().addTabStatic("ledger-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// to Ledger Close Popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    public static <T> void openLedgerPopUpForPurchaseInvoice(Stage stage, String ledger_id, String title, Consumer<Object[]> callback, Consumer<Boolean> addLedCallBack) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
//        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label("Filter");
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();

        VBox vboxGvProduct = new VBox();
        vboxGvProduct.setPrefHeight(23);
        vboxGvProduct.setMaxHeight(23);
        vboxGvProduct.setMinHeight(23);
        vboxGvProduct.setPrefWidth(35.5);
        vboxGvProduct.setMinWidth(35.5);
        vboxGvProduct.setMaxWidth(35.5);
        vboxGvProduct.setStyle("-fx-background-color: #f8f4fc; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #f8f4fc; -fx-border-width: 2;");
        vboxGvProduct.getChildren().add(switchButton);

        switchButton.setParentBox(vboxGvProduct);

        Label titleLabel = new Label(title);
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(vboxGvProduct);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Ledger (Ctrl+N)");
        addButton.setMinWidth(200);
        addButton.setMaxWidth(200);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);


        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[11];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(430);
        tableView.setMaxHeight(430);
        tableView.setMinHeight(430);

        tblLedger = tableView;

        //focus to search field by default in Ledger Popup
        Platform.runLater(() -> {
            if (ledId.equalsIgnoreCase("")) {
                search.requestFocus();
            }

        });
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                } else if (event.isControlDown() && event.getCode() == KeyCode.N) {
                    GlobalController.getInstance().addTabStatic("ledger-create", false);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);

                } else if (event.isControlDown() && event.getCode() == KeyCode.E) {
                    TranxLedgerWindowDTO selectedItem = (TranxLedgerWindowDTO) tableView.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        System.err.println("No item selected in the table.");
                        return;
                    }
                    String LedgerId = selectedItem.getId(); // Ledger Id
                    System.out.println("selectedItem.getId() " + LedgerId);
                    GlobalController.getInstance().addTabStaticWithParam("ledger-edit", false, Integer.valueOf(LedgerId));
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            }
        });


        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                tableView.setItems(observableListLedger);
            }
        });

        /*  Cursor Movements start  */
        addButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.TAB && keyEvent.isShiftDown()) {
                switchButton.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.TAB && !keyEvent.isShiftDown() || keyEvent.getCode() == KeyCode.ENTER) {
                search.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                search.requestFocus();
            }
        });

        switchButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.TAB) {
                addButton.requestFocus();
            }
        });

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                addButton.requestFocus();
            } else if (event.getCode() == KeyCode.TAB && !event.isShiftDown() || event.getCode() == KeyCode.ENTER) {
                tableView.requestFocus();
            }
        });

        /*  Cursor Movements end  */

        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        //   fetchDataOfAllTransactionLedgers("", "");
        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);

        switchButton.switchOnProperty().set(true);

        if (switchButton.switchOnProperty().get()) {
            if (title.equalsIgnoreCase("debtors")) {
                fetchDataOfAllTransactionLedgers("", "SD");

            } else if (title.equalsIgnoreCase("creditors")) {
                fetchDataOfAllTransactionLedgers("", "SC");

            } else {
                fetchDataOfAllTransactionLedgers("", "");

            }
        }

        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                if (title.equalsIgnoreCase("debtors"))
                    fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                else if (title.equalsIgnoreCase("creditors")) {
                    fetchDataOfAllTransactionLedgers("", "SC"); // Fetch only SD type ledgers
                } else fetchDataOfAllTransactionLedgers("", "");

            } else {
                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
                System.out.println("test 2");
            }
        });
        tableView.setItems(observableListLedger);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
       /* search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });*/

        List<TranxLedgerWindowDTO> list = tableView.getItems();

        int index = -1;
        int count = 0;
        for (TranxLedgerWindowDTO tranxLedgerWindowDTO : list) {
            if (tranxLedgerWindowDTO.getId().equals(ledger_id)) {
                index = count;
            }
            count++;
        }

        int finalIndex = index;
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(finalIndex);
            tableView.scrollTo(finalIndex);
        });


        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();

                        System.out.println(ledgerWindowDTO.getSalesmanId());
//                        ObservableList<SalesmanMasterDTO> salesManDetails = ledgerWindowDTO.getSalesmanId();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getStateCode();
                        selectedItem[4] = ledgerWindowDTO.getType();
                        selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                        selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                        selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                        selectedItem[9] = ledgerWindowDTO.getPendingChallans();
                        callback.accept(selectedItem);
                        gstDetails.clear();
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            row.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {

                    TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                    System.out.println("Ledger Selection-->" + ledgerWindowDTO.getStateCode());
                    ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                    selectedItem[0] = ledgerWindowDTO.getLedger_name();
                    selectedItem[1] = ledgerWindowDTO.getId();
                    selectedItem[2] = gstDetails;
                    selectedItem[3] = ledgerWindowDTO.getStateCode();
                    selectedItem[4] = ledgerWindowDTO.getType();
                    selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                    selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                    selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                    selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                    selectedItem[9] = ledgerWindowDTO.getPendingChallans();

                    callback.accept(selectedItem);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            });
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println(".0" +
                        "" +
                        " Selection-->");
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getStateCode();
                selectedItem[4] = ledgerWindowDTO.getType();
                selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                selectedItem[9] = ledgerWindowDTO.getPendingChallans();
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addLedCallBack.accept(true);
//                GlobalController.getInstance().addTabStatic("ledger-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// to Ledger Close Popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }


    //Ledger Modal
    public static <T> void openLedgerPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
//        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label("Filter");
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();

        VBox vboxGvProduct = new VBox();
        vboxGvProduct.setPrefHeight(23);
        vboxGvProduct.setMaxHeight(23);
        vboxGvProduct.setMinHeight(23);
        vboxGvProduct.setPrefWidth(35.5);
        vboxGvProduct.setMinWidth(35.5);
        vboxGvProduct.setMaxWidth(35.5);
        vboxGvProduct.setStyle("-fx-background-color: #f8f4fc; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #f8f4fc; -fx-border-width: 2;");
        vboxGvProduct.getChildren().add(switchButton);

        switchButton.setParentBox(vboxGvProduct);

        Label titleLabel = new Label(title);
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(vboxGvProduct);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Ledger (ctrl + N)");
        addButton.setMinWidth(200);
        addButton.setMaxWidth(200);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);


        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(430);
        tableView.setMaxHeight(430);
        tableView.setMinHeight(430);

        tblLedger = tableView;

        //focus to search field by default in Ledger Popup
        Platform.runLater(() -> {
            if (ledId.equalsIgnoreCase("")) {
                search.requestFocus();
            }

        });





       /* search.setOnKeyPressed(event -> {
            System.out.println("Search Key--->" + search.getText());
            fetchDataOfAllTransactionLedgers(search.getText(), "");
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN)
                tableView.getSelectionModel().selectFirst();
            tableView.requestFocus();
            event.consume();
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                } else if (event.isControlDown() && event.getCode() == KeyCode.N) {
                    GlobalController.getInstance().addTabStatic("ledger-create", false);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);

                } else if (event.isControlDown() && event.getCode() == KeyCode.E) {
                    TranxLedgerWindowDTO selectedItem = (TranxLedgerWindowDTO) tableView.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        System.err.println("No item selected in the table.");
                        return;
                    }
                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                tableView.setItems(observableListLedger);
            }
        });

//        search.setOnKeyPressed(event->{
//            if(event.getCode() == KeyCode.TAB && !event.isShiftDown()){
//                addButton.requestFocus();
//            }
//        });

//        addButton.setOnKeyPressed(event->{
//            if(event.getCode() == KeyCode.TAB && event.isShiftDown()){
//                search.requestFocus();
//            }
//        });

        /*  Cursor Movements start  */
        addButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.TAB && keyEvent.isShiftDown()) {
                switchButton.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.TAB && !keyEvent.isShiftDown() || keyEvent.getCode() == KeyCode.ENTER) {
                search.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                search.requestFocus();
            }
        });

        switchButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.TAB) {
                addButton.requestFocus();
            }
        });

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                addButton.requestFocus();
            } else if (event.getCode() == KeyCode.TAB && !event.isShiftDown() || event.getCode() == KeyCode.ENTER) {
                tableView.requestFocus();
            }
        });


        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        //   fetchDataOfAllTransactionLedgers("", "");
        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);

        switchButton.switchOnProperty().set(true);

        if (switchButton.switchOnProperty().get()) {
            if (title.equalsIgnoreCase("debtors")) {
                fetchDataOfAllTransactionLedgers("", "SD");
                System.out.println("test 1");
            } else if (title.equalsIgnoreCase("creditors")) {
                fetchDataOfAllTransactionLedgers("", "SC");
                System.out.println("test 2");
            } else {
                fetchDataOfAllTransactionLedgers("", "");
                System.out.println("test 3");
            }
        }


        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                if (title.equalsIgnoreCase("debtors"))
                    fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                else if (title.equalsIgnoreCase("creditors")) {
                    fetchDataOfAllTransactionLedgers("", "SC"); // Fetch only SD type ledgers
                } else fetchDataOfAllTransactionLedgers("", "");

            } else {
                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
                System.out.println("test 2");
            }
        });
        tableView.setItems(observableListLedger);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
       /* search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });*/

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();

                        System.out.println(ledgerWindowDTO.getSalesmanId());
//                        ObservableList<SalesmanMasterDTO> salesManDetails = ledgerWindowDTO.getSalesmanId();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getStateCode();
                        selectedItem[4] = ledgerWindowDTO.getType();
                        selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                        selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                        selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                        selectedItem[9] = ledgerWindowDTO.getPendingChallans();

                        callback.accept(selectedItem);
                        gstDetails.clear();
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            row.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {

                    TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                    System.out.println("Ledger Selection-->" + ledgerWindowDTO.getStateCode());
                    ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                    selectedItem[0] = ledgerWindowDTO.getLedger_name();
                    selectedItem[1] = ledgerWindowDTO.getId();
                    selectedItem[2] = gstDetails;
                    selectedItem[3] = ledgerWindowDTO.getStateCode();
                    selectedItem[4] = ledgerWindowDTO.getType();
                    selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                    selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                    callback.accept(selectedItem);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            });
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println(".0" +
                        "" +
                        " Selection-->");
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getStateCode();
                selectedItem[4] = ledgerWindowDTO.getType();
                selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GlobalController.getInstance().addTabStatic("ledger-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);

            }
        });
// to Ledger Close Popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    public static <T> void openLedgerPopUp(Stage stage, String title, Consumer<Object[]> callback, Consumer<Boolean> addLedCallBack) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
//        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();
        VBox vboxGvProduct = new VBox();
        vboxGvProduct.setPrefHeight(23);
        vboxGvProduct.setMaxHeight(23);
        vboxGvProduct.setMinHeight(23);
        vboxGvProduct.setPrefWidth(35.5);
        vboxGvProduct.setMinWidth(35.5);
        vboxGvProduct.setMaxWidth(35.5);
        vboxGvProduct.setStyle("-fx-background-color: #f8f4fc; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #f8f4fc; -fx-border-width: 2;");
        vboxGvProduct.getChildren().add(switchButton);

        switchButton.setParentBox(vboxGvProduct);
        Label titleLabel = new Label(title);
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(vboxGvProduct);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("Add Ledger (Ctrl+N)");
        addButton.setMinWidth(200);
        addButton.setMaxWidth(200);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);


        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        /*tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);*/

        tblLedger = tableView;

        //focus to search field by default in Ledger Popup
        Platform.runLater(() -> {
            if (ledId.equalsIgnoreCase("")) {
                search.requestFocus();
            }

        });
       /* search.setOnKeyPressed(event -> {
            System.out.println("Search Key--->" + search.getText());
            fetchDataOfAllTransactionLedgers(search.getText(), "");
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN)
                tableView.getSelectionModel().selectFirst();
            tableView.requestFocus();
            event.consume();
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

//                if(event.isControlDown()){
//                    addButton.requestFocus();
                if (event.isControlDown() && event.getCode() == KeyCode.N) {
                    addButton.fire();
//                    }
//                    event.consume();
                } else if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                } else if (event.isControlDown() && event.getCode() == KeyCode.E) {
                    TranxLedgerWindowDTO selectedItem = (TranxLedgerWindowDTO) tableView.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        System.err.println("No item selected in the table.");
                        return;
                    }
                    String LedgerId = selectedItem.getId(); // Ledger Id
                    System.out.println("selectedItem.getId() " + LedgerId);
                    GlobalController.getInstance().addTabStaticWithParam("ledger-edit", false, Integer.valueOf(LedgerId));
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
//                    event.consume();
                }
            }
        });


        /*  Cursor Movements start  */
        addButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.TAB && keyEvent.isShiftDown()) {
                switchButton.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.TAB && !keyEvent.isShiftDown() || keyEvent.getCode() == KeyCode.ENTER) {
                search.requestFocus();
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                search.requestFocus();
            }
        });

        switchButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.TAB) {
                addButton.requestFocus();
            }
        });

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                addButton.requestFocus();
            } else if (event.getCode() == KeyCode.TAB && !event.isShiftDown() || event.getCode() == KeyCode.ENTER) {
                tableView.requestFocus();
            }
        });

        /*  Cursor Movements end  */
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                String filterKey = "";
                if (switchButton.switchOnProperty().get())
                    filterKey = "SC";
                else
                    filterKey = "";
                fetchDataOfAllTransactionLedgers(searchKey, filterKey);
                tableView.setItems(observableListLedger);
            }
        });


        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("balance_type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
//        fetchDataOfAllTransactionLedgers("", "");
        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                if (title.equalsIgnoreCase("debtors"))
                    fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                else if (title.equalsIgnoreCase("creditors"))
                    fetchDataOfAllTransactionLedgers("", "SC"); // Fetch only SD type ledgers
                else fetchDataOfAllTransactionLedgers("", "");

            } else {
                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
            }
        });
        switchButton.switchOnProperty().set(true);
        tableView.setItems(observableListLedger);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
       /* search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionLedgers(searchKey, "");
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });*/

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();

                        System.out.println(ledgerWindowDTO.getSalesmanId());
//                        ObservableList<SalesmanMasterDTO> salesManDetails = ledgerWindowDTO.getSalesmanId();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getStateCode();
                        selectedItem[4] = ledgerWindowDTO.getBalance_type();
                        selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                        selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId();
                        selectedItem[8] = ledgerWindowDTO.getPendingOrders();
                        selectedItem[9] = ledgerWindowDTO.getPendingChallans();
                        callback.accept(selectedItem);
                        gstDetails.clear();
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            row.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {

                    TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                    System.out.println("Ledger Selection-->" + ledgerWindowDTO.getStateCode());
                    ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                    selectedItem[0] = ledgerWindowDTO.getLedger_name();
                    selectedItem[1] = ledgerWindowDTO.getId();
                    selectedItem[2] = gstDetails;
                    selectedItem[3] = ledgerWindowDTO.getStateCode();
                    selectedItem[4] = ledgerWindowDTO.getBalance_type();
                    selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                    selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                    callback.accept(selectedItem);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            });
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println(".0" +
                        "" +
                        " Selection-->");
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getStateCode();
                selectedItem[4] = ledgerWindowDTO.getBalance_type();
                selectedItem[5] = ledgerWindowDTO.getBalancingMethod();
                selectedItem[6] = ledgerWindowDTO.getUnder_slug();
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addLedCallBack.accept(true);
//                GlobalController.getInstance().addTabStatic("ledger-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// to Ledger Close Popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    public static <T> void openSalesLedgerPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();
        Label titleLabel = new Label("Debtor");
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(switchButton);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Ledger");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[6];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        fetchDataOfAllTransactionLedgers("", "");
        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
            } else {
                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
            }
        });
        tableView.setItems(observableListLedger);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
        GlobalTranx.requestFocusOrDieTrying(search, 3);
        search.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().select(0);
                tableView.requestFocus();
            }
            e.consume();
        });

        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        tableView.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            } else if (e.getCode() != KeyCode.UP && e.getCode() != KeyCode.DOWN) {
                GlobalTranx.requestFocusOrDieTrying(search, 3);
            }
            e.consume();
        });
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                        selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                        selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                        selectedItem[6] = ledgerWindowDTO.getStateCode(); //? ledger State Code
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }

    public static <T> void openSalesLedgerWithNamePopUp(Stage stage, String title, String inLedgerName, Consumer<Object[]> callback) {

        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.N && e.isControlDown()) {
                GlobalController.getInstance().addTabStatic(PRODUCT_CREATE_SLUG, false);
            }
        });
//        ColorAdjust dim = new ColorAdjust();
//        dim.setBrightness(-0.75);
//        stage.getScene().getRoot().setEffect(dim);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setMaxWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setPrefWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();
        switchButton.setSwitchedOn(true);
        Label titleLabel = new Label("Debtor");
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(switchButton);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Ledger");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[8];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        tableView.setPrefHeight(500);
//        tableView.setMaxHeight(500);
//        tableView.setMinHeight(500);
//        tableView.setMinHeight(GlobalTranx.TranxCalculatePer(TranxCommonPopUps.calculatePopUpHeight(),75));
        tableView.setPrefHeight(TranxCommonPopUps.calculatePopUpHeight());
        tableView.setPrefWidth(TranxCommonPopUps.calculatePopUpWidth());
        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        ObservableList<TranxLedgerWindowDTO> lstTable = lstFetchDataOfAllTransactionLedgers("", "SD");
//        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                ObservableList<TranxLedgerWindowDTO> innlstTable = lstFetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                tableView.setItems(innlstTable);
            } else {
                ObservableList<TranxLedgerWindowDTO> innlstTable = lstFetchDataOfAllTransactionLedgers("", ""); // Fetch only SD type ledgers
                tableView.setItems(innlstTable);
            }
        });
        tableView.setItems(lstTable);

        if (!inLedgerName.trim().isEmpty()) {
            ObservableList<TranxLedgerWindowDTO> lstSearchTable = lstFetchDataOfAllTransactionLedgers(inLedgerName, "");
            if (lstTable.size() > 0) {
                OptionalInt rowIndex = IntStream.range(0, lstTable.size())
                        .filter(i -> lstTable.get(i).getLedger_name().equalsIgnoreCase(inLedgerName))
                        .findFirst();

                if (!rowIndex.isEmpty() && rowIndex.getAsInt() > -1) {
                    tableView.getSelectionModel().select(rowIndex.getAsInt());
                    GlobalTranx.requestFocusOrDieTrying(tableView, 3);
                }
                tableView.setItems(lstSearchTable);
            }
        } else {
            GlobalTranx.requestFocusOrDieTrying(search, 3);
        }
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/


        search.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().select(0);
                tableView.requestFocus();
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }

            e.consume();
        });


        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        tableView.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                selectedItem[6] = ledgerWindowDTO.getStateCode(); // State Code
                selectedItem[7] = ledgerWindowDTO.getSalesmanId(); // SalesMan Id
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            } else if (e.getCode() == KeyCode.BACK_SPACE) {
                GlobalTranx.requestFocusOrDieTrying(search, 3);
            } else if (e.getCode() != KeyCode.UP && e.getCode() != KeyCode.DOWN) {
                GlobalTranx.requestFocusOrDieTrying(search, 3);
            }
            e.consume();
        });
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                        selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                        selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                        selectedItem[6] = ledgerWindowDTO.getStateCode(); // State Code
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId(); // SalesMan Id
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, TranxCommonPopUps.calculatePopUpWidth(), TranxCommonPopUps.calculatePopUpHeight());

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

//        primaryStage.centerOnScreen();
        primaryStage.setX(TranxCommonPopUps.calculatePopUpX());
        primaryStage.setY(TranxCommonPopUps.calculatePopUpY());

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                primaryStage.close();
                GlobalController.getInstance().addTabStatic(PRODUCT_CREATE_SLUG, false);
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        switchButton.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.TAB) {
                addButton.requestFocus();
            }
        });
    }

    /**
     * @ImplNote: Sales Ledger open
     * @author: kirankumar.gadagi
     **/
    public static <T> void openSalesLedgerWithNamePopUp(Stage stage, String title, String inLedgerName, Consumer<Object[]> callback, Consumer<Boolean> addLedCallBack) {

        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
//            if (e.getCode() == KeyCode.N && e.isControlDown()) {
//                GlobalController.getInstance().addTabStatic("ledger-create", false);
//            }
        });
//        ColorAdjust dim = new ColorAdjust();
//        dim.setBrightness(-0.75);
//        stage.getScene().getRoot().setEffect(dim);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setMaxWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setPrefWidth(TranxCommonPopUps.calculatePopUpWidth());
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        SwitchButton switchButton = new SwitchButton();
        VBox vboxGvProduct = new VBox();
        vboxGvProduct.setPrefHeight(23);
        vboxGvProduct.setMaxHeight(23);
        vboxGvProduct.setMinHeight(23);
        vboxGvProduct.setPrefWidth(35.5);
        vboxGvProduct.setMinWidth(35.5);
        vboxGvProduct.setMaxWidth(35.5);
        vboxGvProduct.setStyle("-fx-background-color: #f8f4fc; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #f8f4fc; -fx-border-width: 2;");
        vboxGvProduct.getChildren().add(switchButton);

        switchButton.setParentBox(vboxGvProduct);
        Label titleLabel = new Label("Debtor");
        //! On Open Ledger popup debtor switch button on
        switchButton.switchOnProperty().set(true);
        // Set the font size
        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        hbox_top.getChildren().add(vboxGvProduct);
        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("Add Ledger (Ctrl+N)");
        addButton.setMinWidth(200);
        addButton.setMaxWidth(200);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[8];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxLedgerWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        tableView.setPrefHeight(500);
//        tableView.setMaxHeight(500);
//        tableView.setMinHeight(500);
//        tableView.setMinHeight(GlobalTranx.TranxCalculatePer(TranxCommonPopUps.calculatePopUpHeight(),75));
        tableView.setPrefHeight(TranxCommonPopUps.calculatePopUpHeight());
        tableView.setPrefWidth(TranxCommonPopUps.calculatePopUpWidth());
        TableColumn<TranxLedgerWindowDTO, String> ledgerCode = new TableColumn<>("Code");
        TableColumn<TranxLedgerWindowDTO, String> ledgerName = new TableColumn<>("Ledger Name");
        TableColumn<TranxLedgerWindowDTO, String> ledgerGroup = new TableColumn<>("Ledger Group");
        TableColumn<TranxLedgerWindowDTO, String> ledgerContactNumber = new TableColumn<>("Contact Number");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerCurrentBalance = new TableColumn<>("Current Balance");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerType = new TableColumn<>("Type");
        TableColumn<TranxLedgerWindowDTO, Void> ledgerActions = new TableColumn<>("Actions");

        tableView.getColumns().addAll(ledgerCode, ledgerName, ledgerGroup, ledgerContactNumber, ledgerCurrentBalance, ledgerType, ledgerActions);

        ledgerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        ledgerName.setCellValueFactory(new PropertyValueFactory<>("ledger_name"));
        ledgerGroup.setCellValueFactory(new PropertyValueFactory<>("ledger_group"));
        ledgerContactNumber.setCellValueFactory(new PropertyValueFactory<>("contact_number"));
        ledgerCurrentBalance.setCellValueFactory(new PropertyValueFactory<>("current_balance"));
        ledgerType.setCellValueFactory(new PropertyValueFactory<>("type"));
       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        ObservableList<TranxLedgerWindowDTO> lstTable = lstFetchDataOfAllTransactionLedgers("", "SD");
//        tableView.setItems(observableListLedger);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            // Check the state of the SwitchButton and apply filter accordingly
            if (newValue) {
                ObservableList<TranxLedgerWindowDTO> innlstTable = lstFetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
                tableView.setItems(innlstTable);
            } else {
                ObservableList<TranxLedgerWindowDTO> innlstTable = lstFetchDataOfAllTransactionLedgers("", ""); // Fetch only SD type ledgers
                tableView.setItems(innlstTable);
            }
        });
        tableView.setItems(lstTable);

        if (!inLedgerName.trim().isEmpty()) {
            ObservableList<TranxLedgerWindowDTO> lstSearchTable = lstFetchDataOfAllTransactionLedgers(inLedgerName, "");
            if (lstTable.size() > 0) {
                OptionalInt rowIndex = IntStream.range(0, lstTable.size())
                        .filter(i -> lstTable.get(i).getLedger_name().equalsIgnoreCase(inLedgerName))
                        .findFirst();

                if (!rowIndex.isEmpty() && rowIndex.getAsInt() > -1) {
                    tableView.getSelectionModel().select(rowIndex.getAsInt());
                    GlobalTranx.requestFocusOrDieTrying(tableView, 3);
                }
                tableView.setItems(lstSearchTable);
            }
        } else {
            GlobalTranx.requestFocusOrDieTrying(search, 3);
        }
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/


        search.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().select(0);
                tableView.requestFocus();
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }

            e.consume();
        });


        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
                if (searchKey.length() >= 3) {
                    fetchDataOfAllTransactionLedgers(searchKey, "");
                } else if (searchKey.isEmpty()) {
                    fetchDataOfAllTransactionLedgers("", "");
                }
                tableView.setItems(observableListLedger);
            }
        });
        //code for focus on first list when key d
        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                tableView.requestFocus();
                tableView.getSelectionModel().select(0);
            }
        });
        switchButton.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.TAB) {
                addButton.requestFocus();
            }
        });
        borderPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);

        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

//                if (event.isControlDown()) {
//                    addButton.requestFocus();
                if (event.isControlDown() && event.getCode() == KeyCode.N) {
                    addButton.fire();
                }
//                    event.consume();
//                }
                else if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                } else if (event.isControlDown() && event.getCode() == KeyCode.E) {
                    TranxLedgerWindowDTO selectedItem = (TranxLedgerWindowDTO) tableView.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        System.err.println("No item selected in the table.");
                        return;
                    }
                    String LedgerId = selectedItem.getId(); // Ledger Id
                    System.out.println("selectedItem.getId() " + LedgerId);
                    GlobalController.getInstance().addTabStaticWithParam("ledger-edit", false, Integer.valueOf(LedgerId));
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                }
            }
        });

        tableView.setOnKeyReleased((e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                selectedItem[0] = ledgerWindowDTO.getLedger_name();
                selectedItem[1] = ledgerWindowDTO.getId();
                selectedItem[2] = gstDetails;
                selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                selectedItem[6] = ledgerWindowDTO.getStateCode(); // State Code
                selectedItem[7] = ledgerWindowDTO.getSalesmanId(); // SalesMan Id
                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            } else if (e.getCode() == KeyCode.BACK_SPACE) {
                GlobalTranx.requestFocusOrDieTrying(search, 3);
            } else if (e.getCode() != KeyCode.UP && e.getCode() != KeyCode.DOWN) {
                GlobalTranx.requestFocusOrDieTrying(search, 3);
            }
            e.consume();
        });
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxLedgerWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
                        selectedItem[1] = ledgerWindowDTO.getId();
                        selectedItem[2] = gstDetails;
                        selectedItem[3] = ledgerWindowDTO.getPendingQuotation(); // Quotation
                        selectedItem[4] = ledgerWindowDTO.getPendingOrders(); // Orders
                        selectedItem[5] = ledgerWindowDTO.getPendingChallans(); // Challans
                        selectedItem[6] = ledgerWindowDTO.getStateCode(); // State Code
                        selectedItem[7] = ledgerWindowDTO.getSalesmanId(); // SalesMan Id
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, TranxCommonPopUps.calculatePopUpWidth(), TranxCommonPopUps.calculatePopUpHeight());

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

//        primaryStage.centerOnScreen();
        primaryStage.setX(TranxCommonPopUps.calculatePopUpX());
        primaryStage.setY(TranxCommonPopUps.calculatePopUpY());

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                addLedCallBack.accept(true);
                primaryStage.close();

                //    GlobalController.getInstance().addTabStatic(PRODUCT_CREATE_SLUG, false);
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }

    //Get ledger List in Ledger Modal
    private static void fetchDataOfAllTransactionLedgers(String searchText, String ledgerTypeFilter) {
        observableListLedger.clear();

        Map<String, String> body = new HashMap<>();
        body.put("search", searchText);

        String formData = Globals.mapToStringforFormData(body);
        HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_TRANX_LEDGER_LIST);

        String responseBody = response.body();
        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
        if (jsonObject.get("responseStatus").getAsInt() == 200) {
            JsonArray responseArray = jsonObject.get("list").getAsJsonArray();
            if (responseArray.size() > 0) {
                int count = 0;
                for (JsonElement element : responseArray) {
                    JsonObject item = element.getAsJsonObject();
                    JSONObject itemOBj = new JSONObject(element.toString());
                    String ledger_group_type = item.get("type").getAsString();
                    // Apply filter based on ledger type if provided
                    if (ledgerTypeFilter.isEmpty() || ledgerTypeFilter.equalsIgnoreCase(ledger_group_type)) {
                        // Proceed only if ledger type matches filter or filter is empty
                        String id = item.get("id").getAsString();
                        String code = item.get("code").getAsString();
                        String unique_code = item.get("unique_code").getAsString();
                        String ledger_name = item.get("ledger_name").getAsString();
                        String ledger_group = "";
                        if (ledger_group_type.equalsIgnoreCase("SC")) {
                            ledger_group = "Creditor";
                        } else if (ledger_group_type.equalsIgnoreCase("SD")) {
                            ledger_group = "Debitor";
                        }
                        String contact_number = item.get("contact_number").getAsString();
                        String current_balance = item.get("current_balance").getAsString();
                        String type = item.get("type").getAsString();
                        String salesmanId = item.get("salesmanId").getAsString();
                        String balancingMethod = item.get("balancingMethod").getAsString();
                        String balance_type = item.get("balance_type").getAsString();
                        String salesRate = item.get("salesRate").getAsString();
                        String stateCode = item.get("stateCode").isJsonNull() ? "" : item.get("stateCode").getAsString();
                        String pendingQuotation = "";
                        String pendingOrders = "";
                        String pendingChallans = "";
                        String underSlug = "";
                        try {
                            pendingQuotation = !itemOBj.has("pending_quotation") ? "" : "" + itemOBj.getInt("pending_quotation");
                            pendingOrders = !itemOBj.has("pending_orders") ? "" : "" + itemOBj.getInt("pending_orders");
                            pendingChallans = !itemOBj.has("pending_challans") ? "" : "" + itemOBj.getInt("pending_challans");
                            underSlug = !itemOBj.has("under_slug") ? "" : itemOBj.getString("under_slug");
                        } catch (Exception e) {
                            logger.error("Exception in GETLEDGERLIST : " + Globals.getExceptionString(e));
                        }
                        // Retrieving the gstDetails property
                        JsonArray gstDetailsJsonArray = item.getAsJsonArray("gstDetails");
                        ObservableList<GstDetailsDTO> gstDetailsList = FXCollections.observableArrayList();
                        for (JsonElement element1 : gstDetailsJsonArray) {
                            JsonObject item1 = element1.getAsJsonObject();
                            gstDetailsList.add(new GstDetailsDTO(item1.get("id").getAsString(), item1.get("gstNo").getAsString(), item1.get("state").getAsString()));
                        }
                        TranxLedgerWindowDTO ledgerWindowDTO = new TranxLedgerWindowDTO();
                        ledgerWindowDTO.setIndex(String.valueOf(count));
                        ledgerWindowDTO.setId(id);
                        ledgerWindowDTO.setCode(code);
                        ledgerWindowDTO.setUnique_code(unique_code);
                        ledgerWindowDTO.setLedger_name(ledger_name);
                        ledgerWindowDTO.setLedger_group(ledger_group);
                        ledgerWindowDTO.setContact_number(contact_number);
                        ledgerWindowDTO.setCurrent_balance(current_balance);
                        ledgerWindowDTO.setType(type);
                        ledgerWindowDTO.setSalesmanId(salesmanId);
                        ledgerWindowDTO.setBalancingMethod(balancingMethod);
                        ledgerWindowDTO.setBalance_type(balance_type);
                        ledgerWindowDTO.setGstDetails(gstDetailsList);
                        ledgerWindowDTO.setSalesRate(salesRate);
                        ledgerWindowDTO.setStateCode(stateCode);
                        ledgerWindowDTO.setPendingQuotation(pendingQuotation);
                        ledgerWindowDTO.setPendingOrders(pendingOrders);
                        ledgerWindowDTO.setPendingChallans(pendingChallans);
                        ledgerWindowDTO.setUnder_slug(underSlug);
                        observableListLedger.add(ledgerWindowDTO);

                    }

                    count++;
                }

                //finding index of selected ledger
                if (ledId != "") {
                    System.out.println("ledId >>>>>>>>>" + ledId);
                    for (TranxLedgerWindowDTO item : observableListLedger) {
                        if (item.getId().equalsIgnoreCase(ledId)) {
                            index1 = observableListLedger.indexOf(item);
                        }
                    }
                    // focus to selected row index in productPopup
                    tblLedger.requestFocus();
                    tblLedger.getSelectionModel().clearAndSelect(index1);
                    tblLedger.scrollTo(index1);
                }
            } else {
                logger.debug("Response is Empty openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()");
            }
        } else {
            logger.debug("Error in response openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()");
        }

    }

    private static ObservableList<TranxLedgerWindowDTO> lstFetchDataOfAllTransactionLedgers(String searchText, String ledgerTypeFilter) {
        observableListLedger.clear();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("search", searchText); // Assuming search is not dependent on ledger type
            //To Send the Data in JSON Format
//            String formData = Globals.mapToString(body);
//            HttpResponse<String> response = APIClient.postJsonRequest(formData, EndPoints.GET_TRANX_LEDGER_LIST);
            //To Send the Data in Form Format
            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_TRANX_LEDGER_LIST);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("list").getAsJsonArray();
                if (responseArray.size() > 0) {
                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
                        JSONObject itemOBj = new JSONObject(element.toString());
                        String ledger_group_type = item.get("type").getAsString();
                        // Apply filter based on ledger type if provided
                        if (ledgerTypeFilter.isEmpty() || ledgerTypeFilter.equalsIgnoreCase(ledger_group_type)) {
                            // Proceed only if ledger type matches filter or filter is empty
                            String id = item.get("id").getAsString();
                            String code = item.get("code").getAsString();
                            String unique_code = item.get("unique_code").getAsString();
                            String ledger_name = item.get("ledger_name").getAsString();
                            String ledger_group = "";
                            if (ledger_group_type.equalsIgnoreCase("SC")) {
                                ledger_group = "Creditor";
                            } else if (ledger_group_type.equalsIgnoreCase("SD")) {
                                ledger_group = "Debitor";
                            }
                            String contact_number = item.get("contact_number").getAsString();
                            String current_balance = item.get("current_balance").getAsString();
                            String type = item.get("type").getAsString();
                            String salesmanId = item.get("salesmanId").getAsString();
                            String balancingMethod = item.get("balancingMethod").getAsString();
                            String balance_type = item.get("balance_type").getAsString();
                            String salesRate = item.get("salesRate").getAsString();
                            String stateCode = item.get("stateCode").isJsonNull() ? "" : item.get("stateCode").getAsString();
                            String pendingQuotation = "";
                            String pendingOrders = "";
                            String pendingChallans = "";
                            String underSlug = "";
                            try {
                                pendingQuotation = !itemOBj.has("pending_quotation") ? "" : "" + itemOBj.getInt("pending_quotation");
                                pendingOrders = !itemOBj.has("pending_orders") ? "" : "" + itemOBj.getInt("pending_orders");
                                pendingChallans = !itemOBj.has("pending_challans") ? "" : "" + itemOBj.getInt("pending_challans");
                                underSlug = !itemOBj.has("under_slug") ? "" : itemOBj.getString("under_slug");
                            } catch (Exception e) {
                                logger.error("Exception in GETLEDGERLIST : " + Globals.getExceptionString(e));
                            }
                            // Retrieving the gstDetails property
                            JsonArray gstDetailsJsonArray = item.getAsJsonArray("gstDetails");
                            ObservableList<GstDetailsDTO> gstDetailsList = FXCollections.observableArrayList();
                            for (JsonElement element1 : gstDetailsJsonArray) {
                                JsonObject item1 = element1.getAsJsonObject();
                                gstDetailsList.add(new GstDetailsDTO(item1.get("id").getAsString(), item1.get("gstNo").getAsString(), item1.get("state").getAsString()));
                            }
                            TranxLedgerWindowDTO ledgerWindowDTO = new TranxLedgerWindowDTO();
                            ledgerWindowDTO.setId(id);
                            ledgerWindowDTO.setCode(code);
                            ledgerWindowDTO.setUnique_code(unique_code);
                            ledgerWindowDTO.setLedger_name(ledger_name);
                            ledgerWindowDTO.setLedger_group(ledger_group);
                            ledgerWindowDTO.setContact_number(contact_number);
                            ledgerWindowDTO.setCurrent_balance(current_balance);
                            ledgerWindowDTO.setType(type);
                            ledgerWindowDTO.setSalesmanId(salesmanId);
                            ledgerWindowDTO.setBalancingMethod(balancingMethod);
                            ledgerWindowDTO.setBalance_type(balance_type);
                            ledgerWindowDTO.setGstDetails(gstDetailsList);
                            ledgerWindowDTO.setSalesRate(salesRate);
                            ledgerWindowDTO.setStateCode(stateCode);
                            ledgerWindowDTO.setPendingQuotation(pendingQuotation);
                            ledgerWindowDTO.setPendingOrders(pendingOrders);
                            ledgerWindowDTO.setPendingChallans(pendingChallans);
                            ledgerWindowDTO.setUnder_slug(underSlug);
                            observableListLedger.add(ledgerWindowDTO);
                        }
                    }

                } else {
                    logger.debug("Response is Empty openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()");
                }
            } else {
                logger.debug("Error in response openLedgerPopUp()---->lstFetchDataOfAllTransactionLedgers()");
            }
            /*APIClient apiClient = new APIClient(EndPoints.GET_TRANX_LEDGER_LIST, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    String responseBody = workerStateEvent.getSource().getValue().toString();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("list").getAsJsonArray();
                        if (responseArray.size() > 0) {
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                JSONObject itemOBj = new JSONObject(element.toString());
                                String ledger_group_type = item.get("type").getAsString();
                                // Apply filter based on ledger type if provided
                                if (ledgerTypeFilter.isEmpty() || ledgerTypeFilter.equalsIgnoreCase(ledger_group_type)) {
                                    // Proceed only if ledger type matches filter or filter is empty
                                    String id = item.get("id").getAsString();
                                    String code = item.get("code").getAsString();
                                    String unique_code = item.get("unique_code").getAsString();
                                    String ledger_name = item.get("ledger_name").getAsString();
                                    String ledger_group = "";
                                    if (ledger_group_type.equalsIgnoreCase("SC")) {
                                        ledger_group = "Creditor";
                                    } else if (ledger_group_type.equalsIgnoreCase("SD")) {
                                        ledger_group = "Debitor";
                                    }
                                    String contact_number = item.get("contact_number").getAsString();
                                    String current_balance = item.get("current_balance").getAsString();
                                    String type = item.get("type").getAsString();
                                    String salesmanId = item.get("salesmanId").getAsString();
                                    String balancingMethod = item.get("balancingMethod").getAsString();
                                    String balance_type = item.get("balance_type").getAsString();
                                    String salesRate = item.get("salesRate").getAsString();
                                    String stateCode = item.get("stateCode").isJsonNull() ? "" : item.get("stateCode").getAsString();
                                    String pendingQuotation = "";
                                    String pendingOrders = "";
                                    String pendingChallans = "";
                                    String underSlug = "";
                                    try {
                                        pendingQuotation = !itemOBj.has("pending_quotation") ? "" : "" + itemOBj.getInt("pending_quotation");
                                        pendingOrders = !itemOBj.has("pending_orders") ? "" : "" + itemOBj.getInt("pending_orders");
                                        pendingChallans = !itemOBj.has("pending_challans") ? "" : "" + itemOBj.getInt("pending_challans");
                                        underSlug = !itemOBj.has("under_slug") ? "" : itemOBj.getString("under_slug");
                                    } catch (Exception e) {
                                        logger.error("Exception in GETLEDGERLIST : " + Globals.getExceptionString(e));
                                    }
                                    // Retrieving the gstDetails property
                                    JsonArray gstDetailsJsonArray = item.getAsJsonArray("gstDetails");
                                    ObservableList<GstDetailsDTO> gstDetailsList = FXCollections.observableArrayList();
                                    for (JsonElement element1 : gstDetailsJsonArray) {
                                        JsonObject item1 = element1.getAsJsonObject();
                                        gstDetailsList.add(new GstDetailsDTO(item1.get("id").getAsString(), item1.get("gstNo").getAsString(), item1.get("state").getAsString()));
                                    }
                                    TranxLedgerWindowDTO ledgerWindowDTO = new TranxLedgerWindowDTO();
                                    ledgerWindowDTO.setId(id);
                                    ledgerWindowDTO.setCode(code);
                                    ledgerWindowDTO.setUnique_code(unique_code);
                                    ledgerWindowDTO.setLedger_name(ledger_name);
                                    ledgerWindowDTO.setLedger_group(ledger_group);
                                    ledgerWindowDTO.setContact_number(contact_number);
                                    ledgerWindowDTO.setCurrent_balance(current_balance);
                                    ledgerWindowDTO.setType(type);
                                    ledgerWindowDTO.setSalesmanId(salesmanId);
                                    ledgerWindowDTO.setBalancingMethod(balancingMethod);
                                    ledgerWindowDTO.setBalance_type(balance_type);
                                    ledgerWindowDTO.setGstDetails(gstDetailsList);
                                    ledgerWindowDTO.setSalesRate(salesRate);
                                    ledgerWindowDTO.setStateCode(stateCode);
                                    ledgerWindowDTO.setPendingQuotation(pendingQuotation);
                                    ledgerWindowDTO.setPendingOrders(pendingOrders);
                                    ledgerWindowDTO.setPendingChallans(pendingChallans);
                                    ledgerWindowDTO.setUnder_slug(underSlug);
                                    observableListLedger.add(ledgerWindowDTO);
                                }
                            }

                        } else {
                            logger.debug("Response is Empty openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()");
                        }
                    } else {
                        logger.debug("Error in response openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()");
                    }
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("onFailed in fetchDataOfAllTransactionLedgers()" +
                            workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("onCancelled in fetchDataOfAllTransactionLedgers()" +
                            workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();*/
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in openLedgerPopUp()---->fetchDataOfAllTransactionLedgers()" + exceptionAsString);
            e.printStackTrace();
        }
        return observableListLedger;
    }


    private static ObservableList<TranxReceiptWindowDTO> fetchDataOfbillByBill(String ledgerId, String cnd) {
        observableBillByBill.clear();
        ObservableList<TranxReceiptWindowDTO> lstBillByBill = FXCollections.observableArrayList();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("ledger_id", "" + ledgerId);
            body.put("type", "SD");
            body.put("balancing_method", "bill-by-bill");
            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_DEBTORS_PENDING_BILLS);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println("response" + response);
            System.out.println("jsonObject" + jsonObject);
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("list").getAsJsonArray();
                if (responseArray.size() > 0) {
                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
                        BillByBillRes billByBillRes = new Gson().fromJson(item, BillByBillRes.class);
                        if (cnd.equalsIgnoreCase(billByBillRes.getSource())) {
                            lstBillByBill.add(new TranxReceiptWindowDTO(billByBillRes.getId(), billByBillRes.getInvoiceId(), billByBillRes.getInvoiceUniqueId(), DateConvertUtil.convertDispDateFormat(billByBillRes.getInvoice_date()), billByBillRes.getInvoice_no(), billByBillRes.getLedger_id(), billByBillRes.getSource(), billByBillRes.getDue_days() != null ? String.valueOf(billByBillRes.getDue_days()) : "", billByBillRes.getBalancing_type(), billByBillRes.getPaid_amt() != null ? billByBillRes.getPaid_amt() : 0.0, billByBillRes.getBill_details_id(), billByBillRes.getRemaining_amt() != null ? billByBillRes.getRemaining_amt() : 0.0, billByBillRes.getAmount(), billByBillRes.getTotal_amt()));
                        }
                    }

                } else {
                    logger.debug("Response is Empty in  fetchDataOfbillByBill()");
                }
            } else {
                logger.debug("Error in response fetchDataOfbillByBill()");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in fetchDataOfbillByBill()----->" + exceptionAsString);
            e.printStackTrace();
        }
        return lstBillByBill;
    }

    public static <T> void openProductPopUpForPurchase(Stage stage, String p_id, String title, Consumer<Object[]> callback, Consumer<Boolean> addPrdCallback) {
        System.out.println("productId-------" + productId);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxProductWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Product");
        addButton.setMinWidth(130);
        addButton.setMaxWidth(130);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("submit-btn");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                addButton.requestFocus();
            }
        });

        addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                search.requestFocus();
            }
        });

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxProductWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(428);
        tableView.setMaxHeight(428);
        tableView.setMinHeight(428);
        TableColumn<TranxProductWindowDTO, String> productCode = new TableColumn<>("Code");
        TableColumn<TranxProductWindowDTO, String> productName = new TableColumn<>("Product Name");
        TableColumn<TranxProductWindowDTO, String> productPacking = new TableColumn<>("Packing");
        TableColumn<TranxProductWindowDTO, String> productBarcode = new TableColumn<>("Barcode");
        TableColumn<TranxProductWindowDTO, Void> productBrand = new TableColumn<>("Brand");
        TableColumn<TranxProductWindowDTO, Void> productMrp = new TableColumn<>("Mrp");
        TableColumn<TranxProductWindowDTO, Void> productCurrentStock = new TableColumn<>("Current Stock");
        TableColumn<TranxProductWindowDTO, Void> productUnit = new TableColumn<>("Unit");
        TableColumn<TranxProductWindowDTO, Void> productSalesRate = new TableColumn<>("Sales Rate");
        TableColumn<TranxProductWindowDTO, Void> productActions = new TableColumn<>("Actions");
        // Adjusting the width for the Columns
        productCode.setPrefWidth(40);
        productName.setPrefWidth(200);
        productPacking.setPrefWidth(50);
        productBarcode.setPrefWidth(50);
        productBrand.setPrefWidth(50);
        productMrp.setPrefWidth(50);
        productUnit.setPrefWidth(50);
        productActions.setPrefWidth(40);
        tableView.getColumns().addAll(productCode, productName, productPacking, productBarcode, productBrand, productMrp, productCurrentStock, productUnit, productSalesRate, productActions);

        productCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        productName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        productPacking.setCellValueFactory(new PropertyValueFactory<>("packing"));
        productBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        productBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        productMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        productCurrentStock.setCellValueFactory(new PropertyValueFactory<>("current_stock"));
        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        productSalesRate.setCellValueFactory(new PropertyValueFactory<>("sales_rate"));

        fetchDataOfAllTransactionProducts("");
        tableView.setItems(observableListProduct);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableListProduct);

        // focus to selected row index in productPopup
        if (!productId.isEmpty()) {
            tableView.getSelectionModel().clearAndSelect(index2);

            Platform.runLater(() -> {
                // Scroll to the selected index after the TableView is fully rendered
                tableView.requestFocus();
                tableView.scrollTo(index2);
            });
        } else {
            Platform.runLater(() -> {
                search.requestFocus();
            });
        }
        /*search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            }
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionProducts(searchKey);
                tableView.setItems(observableListProduct);
            }
        });

        List<TranxProductWindowDTO> list = tableView.getItems();

        int index = -1;
        int count = 0;
        System.out.println("p id : " + p_id);


        for (TranxProductWindowDTO tranxProductWindowDTO : list) {

            if (tranxProductWindowDTO.getProduct_name().equals(p_id)) {
                index = count;
            }
            count++;
        }

        int finalIndex = index;
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(finalIndex);
            tableView.scrollTo(finalIndex);
        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxProductWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        productIdSelected = productWindowDTO.getId();
                        selectedItem[0] = productWindowDTO.getProduct_name();
                        selectedItem[1] = productWindowDTO.getId();
                        selectedItem[2] = productWindowDTO.getPacking();
                        selectedItem[3] = productWindowDTO.getMrp();
                        selectedItem[4] = productWindowDTO.getUnit();
                        selectedItem[5] = productWindowDTO.getTax_per();
                        selectedItem[6] = productWindowDTO.getSales_rate();
                        selectedItem[7] = productWindowDTO.getPurchaserate();
                        selectedItem[8] = productWindowDTO.getIs_batch();
                        selectedItem[9] = productWindowDTO.getUnitRateList();
                    /*    List<UnitRateList> list = productWindowDTO.getUnitRateList();
                        System.out.println("Unit Rate List:" + list);*/
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });    //select Product from product popup on Enter
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println("Product Selection-->");
                TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                productIdSelected = productWindowDTO.getId();
                selectedItem[0] = productWindowDTO.getProduct_name();
                selectedItem[1] = productWindowDTO.getId();
                selectedItem[2] = productWindowDTO.getPacking();
                selectedItem[3] = productWindowDTO.getMrp();
                selectedItem[4] = productWindowDTO.getUnit();
                selectedItem[5] = productWindowDTO.getTax_per();
                selectedItem[6] = productWindowDTO.getSales_rate();
                selectedItem[7] = productWindowDTO.getPurchaserate();
                selectedItem[8] = productWindowDTO.getIs_batch();
                selectedItem[9] = productWindowDTO.getUnitRateList();

                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addPrdCallback.accept(true);
//                GlobalController.getInstance().addTabStatic("product-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// close Product popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    public static <T> void openProductPopUpForPurchase(Stage stage, String p_id, String title, Consumer<Object[]> callback) {
        System.out.println("productId-------" + productId);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxProductWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Product");
        addButton.setMinWidth(130);
        addButton.setMaxWidth(130);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("submit-btn");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                addButton.requestFocus();
            }
        });

        addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                search.requestFocus();
            }
        });

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxProductWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(428);
        tableView.setMaxHeight(428);
        tableView.setMinHeight(428);
        TableColumn<TranxProductWindowDTO, String> productCode = new TableColumn<>("Code");
        TableColumn<TranxProductWindowDTO, String> productName = new TableColumn<>("Product Name");
        TableColumn<TranxProductWindowDTO, String> productPacking = new TableColumn<>("Packing");
        TableColumn<TranxProductWindowDTO, String> productBarcode = new TableColumn<>("Barcode");
        TableColumn<TranxProductWindowDTO, Void> productBrand = new TableColumn<>("Brand");
        TableColumn<TranxProductWindowDTO, Void> productMrp = new TableColumn<>("Mrp");
        TableColumn<TranxProductWindowDTO, Void> productCurrentStock = new TableColumn<>("Current Stock");
        TableColumn<TranxProductWindowDTO, Void> productUnit = new TableColumn<>("Unit");
        TableColumn<TranxProductWindowDTO, Void> productSalesRate = new TableColumn<>("Sales Rate");
        TableColumn<TranxProductWindowDTO, Void> productActions = new TableColumn<>("Actions");
        // Adjusting the width for the Columns
        productCode.setPrefWidth(40);
        productName.setPrefWidth(200);
        productPacking.setPrefWidth(50);
        productBarcode.setPrefWidth(50);
        productBrand.setPrefWidth(50);
        productMrp.setPrefWidth(50);
        productUnit.setPrefWidth(50);
        productActions.setPrefWidth(40);
        tableView.getColumns().addAll(productCode, productName, productPacking, productBarcode, productBrand, productMrp, productCurrentStock, productUnit, productSalesRate, productActions);

        productCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        productName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        productPacking.setCellValueFactory(new PropertyValueFactory<>("packing"));
        productBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        productBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        productMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        productCurrentStock.setCellValueFactory(new PropertyValueFactory<>("current_stock"));
        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        productSalesRate.setCellValueFactory(new PropertyValueFactory<>("sales_rate"));

        fetchDataOfAllTransactionProducts("");
        tableView.setItems(observableListProduct);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableListProduct);

        // focus to selected row index in productPopup
        if (!productId.isEmpty()) {
            tableView.getSelectionModel().clearAndSelect(index2);

            Platform.runLater(() -> {
                // Scroll to the selected index after the TableView is fully rendered
                tableView.requestFocus();
                tableView.scrollTo(index2);
            });
        } else {
            Platform.runLater(() -> {
                search.requestFocus();
            });
        }
        /*search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            }
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionProducts(searchKey);
                tableView.setItems(observableListProduct);
            }
        });

        List<TranxProductWindowDTO> list = tableView.getItems();

        int index = -1;
        int count = 0;
        System.out.println("p id : " + p_id);


        for (TranxProductWindowDTO tranxProductWindowDTO : list) {

            if (tranxProductWindowDTO.getProduct_name().equals(p_id)) {
                index = count;
            }
            count++;
        }

        int finalIndex = index;
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(finalIndex);
            tableView.scrollTo(finalIndex);
        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxProductWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        productIdSelected = productWindowDTO.getId();
                        selectedItem[0] = productWindowDTO.getProduct_name();
                        selectedItem[1] = productWindowDTO.getId();
                        selectedItem[2] = productWindowDTO.getPacking();
                        selectedItem[3] = productWindowDTO.getMrp();
                        selectedItem[4] = productWindowDTO.getUnit();
                        selectedItem[5] = productWindowDTO.getTax_per();
                        selectedItem[6] = productWindowDTO.getSales_rate();
                        selectedItem[7] = productWindowDTO.getPurchaserate();
                        selectedItem[8] = productWindowDTO.getIs_batch();
                        selectedItem[9] = productWindowDTO.getUnitRateList();
                    /*    List<UnitRateList> list = productWindowDTO.getUnitRateList();
                        System.out.println("Unit Rate List:" + list);*/
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });    //select Product from product popup on Enter
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println("Product Selection-->");
                TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                productIdSelected = productWindowDTO.getId();
                selectedItem[0] = productWindowDTO.getProduct_name();
                selectedItem[1] = productWindowDTO.getId();
                selectedItem[2] = productWindowDTO.getPacking();
                selectedItem[3] = productWindowDTO.getMrp();
                selectedItem[4] = productWindowDTO.getUnit();
                selectedItem[5] = productWindowDTO.getTax_per();
                selectedItem[6] = productWindowDTO.getSales_rate();
                selectedItem[7] = productWindowDTO.getPurchaserate();
                selectedItem[8] = productWindowDTO.getIs_batch();
                selectedItem[9] = productWindowDTO.getUnitRateList();

                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GlobalController.getInstance().addTabStatic("product-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// close Product popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }


    //Product Modal
    public static <T> void openProductPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        System.out.println("productId-------" + productId);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxProductWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        Button addButton = new Button("Add Product (Ctrl+N)");
        addButton.setMinWidth(200);
        addButton.setMaxWidth(200);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                addButton.requestFocus();
            }
        });

        addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                search.requestFocus();
            }
        });

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxProductWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(428);
        tableView.setMaxHeight(428);
        tableView.setMinHeight(428);
        TableColumn<TranxProductWindowDTO, String> productCode = new TableColumn<>("Code");
        TableColumn<TranxProductWindowDTO, String> productName = new TableColumn<>("Product Name");
        TableColumn<TranxProductWindowDTO, String> productPacking = new TableColumn<>("Packing");
        TableColumn<TranxProductWindowDTO, String> productBarcode = new TableColumn<>("Barcode");
        TableColumn<TranxProductWindowDTO, Void> productBrand = new TableColumn<>("Brand");
        TableColumn<TranxProductWindowDTO, Void> productMrp = new TableColumn<>("Mrp");
        TableColumn<TranxProductWindowDTO, Void> productCurrentStock = new TableColumn<>("Current Stock");
        TableColumn<TranxProductWindowDTO, Void> productUnit = new TableColumn<>("Unit");
        TableColumn<TranxProductWindowDTO, Void> productSalesRate = new TableColumn<>("Sales Rate");
        TableColumn<TranxProductWindowDTO, Void> productActions = new TableColumn<>("Actions");
        // Adjusting the width for the Columns
        productCode.setPrefWidth(40);
        productName.setPrefWidth(200);
        productPacking.setPrefWidth(50);
        productBarcode.setPrefWidth(50);
        productBrand.setPrefWidth(50);
        productMrp.setPrefWidth(50);
        productUnit.setPrefWidth(50);
        productActions.setPrefWidth(40);
        tableView.getColumns().addAll(productCode, productName, productPacking, productBarcode, productBrand, productMrp, productCurrentStock, productUnit, productSalesRate, productActions);

        productCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        productName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        productPacking.setCellValueFactory(new PropertyValueFactory<>("packing"));
        productBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        productBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        productMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        productCurrentStock.setCellValueFactory(new PropertyValueFactory<>("current_stock"));
        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        productSalesRate.setCellValueFactory(new PropertyValueFactory<>("sales_rate"));

        fetchDataOfAllTransactionProducts("");
        tableView.setItems(observableListProduct);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableListProduct);

        // focus to selected row index in productPopup
        if (!productId.isEmpty()) {
            tableView.getSelectionModel().clearAndSelect(index2);

            Platform.runLater(() -> {
                // Scroll to the selected index after the TableView is fully rendered
                tableView.requestFocus();
                tableView.scrollTo(index2);
            });
        } else {
            Platform.runLater(() -> {
                search.requestFocus();
            });
        }
        /*search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            }
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if (event.isControlDown()) {
                    addButton.requestFocus();
                    if (event.isControlDown() && event.getCode() == KeyCode.N) {
                        addButton.fire();
                    }
                    event.consume();
                } else if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionProducts(searchKey);
                tableView.setItems(observableListProduct);
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxProductWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        productIdSelected = productWindowDTO.getId();
                        selectedItem[0] = productWindowDTO.getProduct_name();
                        selectedItem[1] = productWindowDTO.getId();
                        selectedItem[2] = productWindowDTO.getPacking();
                        selectedItem[3] = productWindowDTO.getMrp();
                        selectedItem[4] = productWindowDTO.getUnit();
                        selectedItem[5] = productWindowDTO.getTax_per();
                        selectedItem[6] = productWindowDTO.getSales_rate();
                        selectedItem[7] = productWindowDTO.getPurchaserate();
                        selectedItem[8] = productWindowDTO.getIs_batch();
                        selectedItem[9] = productWindowDTO.getUnitRateList();
                    /*    List<UnitRateList> list = productWindowDTO.getUnitRateList();
                        System.out.println("Unit Rate List:" + list);*/
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });    //select Product from product popup on Enter
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println("Product Selection-->");
                TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                productIdSelected = productWindowDTO.getId();
                selectedItem[0] = productWindowDTO.getProduct_name();
                selectedItem[1] = productWindowDTO.getId();
                selectedItem[2] = productWindowDTO.getPacking();
                selectedItem[3] = productWindowDTO.getMrp();
                selectedItem[4] = productWindowDTO.getUnit();
                selectedItem[5] = productWindowDTO.getTax_per();
                selectedItem[6] = productWindowDTO.getSales_rate();
                selectedItem[7] = productWindowDTO.getPurchaserate();
                selectedItem[8] = productWindowDTO.getIs_batch();
                selectedItem[9] = productWindowDTO.getUnitRateList();

                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GlobalController.getInstance().addTabStatic("product-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// close Product popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    //Product Modal created for Redirection Purpose This Consumer<Boolean> addPrdCallback is Added
    public static <T> void openProductPopUp(Stage stage, String title, Consumer<Object[]> callback, Consumer<Boolean> addPrdCallback) {
        System.out.println("productId-------" + productId);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxProductWindowDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        Button addButton = new Button("+ Add Product");
        addButton.setMinWidth(130);
        addButton.setMaxWidth(130);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxProductWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxProductWindowDTO, String> productCode = new TableColumn<>("Code");
        TableColumn<TranxProductWindowDTO, String> productName = new TableColumn<>("Product Name");
        TableColumn<TranxProductWindowDTO, String> productPacking = new TableColumn<>("Packing");
        TableColumn<TranxProductWindowDTO, String> productBarcode = new TableColumn<>("Barcode");
        TableColumn<TranxProductWindowDTO, Void> productBrand = new TableColumn<>("Brand");
        TableColumn<TranxProductWindowDTO, Void> productMrp = new TableColumn<>("Mrp");
        TableColumn<TranxProductWindowDTO, Void> productCurrentStock = new TableColumn<>("Current Stock");
        TableColumn<TranxProductWindowDTO, Void> productUnit = new TableColumn<>("Unit");
        TableColumn<TranxProductWindowDTO, Void> productSalesRate = new TableColumn<>("Sales Rate");
        TableColumn<TranxProductWindowDTO, Void> productActions = new TableColumn<>("Actions");
        // Adjusting the width for the Columns
        productCode.setPrefWidth(40);
        productName.setPrefWidth(200);
        productPacking.setPrefWidth(50);
        productBarcode.setPrefWidth(50);
        productBrand.setPrefWidth(50);
        productMrp.setPrefWidth(50);
        productUnit.setPrefWidth(50);
        productActions.setPrefWidth(40);
        tableView.getColumns().addAll(productCode, productName, productPacking, productBarcode, productBrand, productMrp, productCurrentStock, productUnit, productSalesRate, productActions);

        productCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        productName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        productPacking.setCellValueFactory(new PropertyValueFactory<>("packing"));
        productBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        productBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        productMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        productCurrentStock.setCellValueFactory(new PropertyValueFactory<>("current_stock"));
        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        productSalesRate.setCellValueFactory(new PropertyValueFactory<>("sales_rate"));

        fetchDataOfAllTransactionProducts("");
        tableView.setItems(observableListProduct);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableListProduct);

        // focus to selected row index in productPopup
        if (!productId.isEmpty()) {
            tableView.getSelectionModel().clearAndSelect(index2);

            Platform.runLater(() -> {
                // Scroll to the selected index after the TableView is fully rendered
                tableView.requestFocus();
                tableView.scrollTo(index2);
            });
        } else {
            Platform.runLater(() -> {
                search.requestFocus();
            });
        }
        /*search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                tableView.getSelectionModel().selectFirst();
                tableView.requestFocus();
                event.consume();
            }
        });*/
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                }
            }
        });
        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                String searchKey = search.getText().trim();
                fetchDataOfAllTransactionProducts(searchKey);
                tableView.setItems(observableListProduct);
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxProductWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                        productIdSelected = productWindowDTO.getId();
                        selectedItem[0] = productWindowDTO.getProduct_name();
                        selectedItem[1] = productWindowDTO.getId();
                        selectedItem[2] = productWindowDTO.getPacking();
                        selectedItem[3] = productWindowDTO.getMrp();
                        selectedItem[4] = productWindowDTO.getUnit();
                        selectedItem[5] = productWindowDTO.getTax_per();
                        selectedItem[6] = productWindowDTO.getSales_rate();
                        selectedItem[7] = productWindowDTO.getPurchaserate();
                        selectedItem[8] = productWindowDTO.getIs_batch();
                        selectedItem[9] = productWindowDTO.getUnitRateList();
                    /*    List<UnitRateList> list = productWindowDTO.getUnitRateList();
                        System.out.println("Unit Rate List:" + list);*/
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });    //select Product from product popup on Enter
            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                System.out.println("Product Selection-->");
                TranxProductWindowDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
                productIdSelected = productWindowDTO.getId();
                selectedItem[0] = productWindowDTO.getProduct_name();
                selectedItem[1] = productWindowDTO.getId();
                selectedItem[2] = productWindowDTO.getPacking();
                selectedItem[3] = productWindowDTO.getMrp();
                selectedItem[4] = productWindowDTO.getUnit();
                selectedItem[5] = productWindowDTO.getTax_per();
                selectedItem[6] = productWindowDTO.getSales_rate();
                selectedItem[7] = productWindowDTO.getPurchaserate();
                selectedItem[8] = productWindowDTO.getIs_batch();
                selectedItem[9] = productWindowDTO.getUnitRateList();

                callback.accept(selectedItem);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addPrdCallback.accept(true);
//                GlobalController.getInstance().addTabStatic("product-create", false);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
// close Product popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    //Get Product List in Product Modal
    public static void fetchDataOfAllTransactionProducts(String key) {
        observableListProduct.clear();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("search", key);
            body.put("barcode", "");
            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_TRANX_PRODUCT_LIST);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("list").getAsJsonArray();
                if (responseArray.size() > 0) {
                    int count = 0;

                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
                        String index = String.valueOf(count);
                        String id = item.get("id").getAsString();
                        String code = item.get("code").getAsString();
                        String product_name = item.get("product_name").getAsString();
                        System.out.println("Product Name:" + product_name);
                        String packing = item.get("packing").getAsString();
                        // String barcode = item.get("barcode").getAsString();
                        String barcode = "";
                        String brand = item.get("brand").getAsString();
                        String mrp = "";
                        String sales_rate = "";
                        if (item.has("mrp") && !item.get("mrp").isJsonNull())
                            mrp = item.get("mrp").getAsString();
                        else {
                            mrp = "0.00";
                        }
                        //  System.out.println("heeelo"+item.get("current_stock"));
                        String current_stock = item.get("current_stock").getAsString();
                        String unit = item.get("unit").getAsString();
                        if (item.has("sales_rate") && !item.get("sales_rate").isJsonNull())
                            sales_rate = item.get("sales_rate").getAsString();
                        else {
                            sales_rate = "";
                        }
                        String is_negative = item.get("is_negative") != null ? item.get("is_negative").getAsString() : "false";
                        String batch_expiry = item.get("batch_expiry") != null ? item.get("batch_expiry").getAsString() : "";
                        String tax_per = item.get("tax_per").getAsString();
                        String is_batch = item.get("is_batch").getAsString();
                        String purchaserate = item.get("purchaserate") != null ? item.get("purchaserate").getAsString() : "";
                        Integer unit_id = item.get("unit_id") != null ? item.get("unit_id").getAsInt() : 0;
                        JsonArray jsonArray = item.get("unit_lst").getAsJsonArray();
                        List<UnitRateList> unitList = new ArrayList<>();
                        if (jsonArray.size() > 0) {
                            for (JsonElement mElement : jsonArray) {
                                JsonObject mObject = mElement.getAsJsonObject();
                                UnitRateList unitRate = new UnitRateList();
                                System.out.println("Unit Id:" + mObject.get("unitid"));
                                System.out.println("unitName:" + mObject.get("unitName"));
                                System.out.println("unitConv:" + mObject.get("unitConv"));
                                System.out.println("fsrmh:" + mObject.get("fsrmh"));
                                System.out.println("fsrai:" + mObject.get("fsrai"));
                                System.out.println("csrmh:" + mObject.get("csrmh"));
                                System.out.println("csrai:" + mObject.get("csrai"));
                                unitRate.setUnitId(mObject.get("unitid").getAsLong());
                                unitRate.setUnitName(mObject.get("unitName").getAsString());
                                unitRate.setUnitConv(mObject.get("unitConv").getAsDouble());
                                unitRate.setFsrmh(mObject.get("fsrmh").getAsDouble());
                                unitRate.setFsrai(mObject.get("fsrai").getAsDouble());
                                unitRate.setCsrmh(mObject.get("csrmh").getAsDouble());
                                unitRate.setCsrai(mObject.get("csrai").getAsDouble());
                                unitRate.setMrp(mObject.get("mrp") != null ? mObject.get("mrp").getAsDouble() : 0);
                                unitRate.setPurRate(mObject.get("purchaserate") != null ? mObject.get("purchaserate").getAsDouble() : 0);
                                unitList.add(unitRate);
                            }
                        }
                        observableListProduct.add(new TranxProductWindowDTO(index, id, code, product_name, packing, barcode, brand, mrp, current_stock, unit,
                                sales_rate, is_negative, batch_expiry, tax_per, is_batch, purchaserate, unitList));
                        count++;
                    }
                    //finding index of selected product
                    if (!productId.isEmpty()) {
                        System.out.println("productId >>>>>>>>>" + productId);
                        for (TranxProductWindowDTO item : observableListProduct) {
                            System.out.println("item.getId() >>>>>>>>>" + item.getId());
                            if (item.getId().equalsIgnoreCase(productId)) {
                                System.out.println("now focus is ready in Product");
                                index2 = observableListProduct.indexOf(item);
                                System.out.println(".indexOf(item) " + index2);
                                System.out.println(" " + item);
                            }
                        }
                    }


                } else {
                    System.out.println("responseObject is null");
                }
            } else {
                System.out.println("Error in response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Product Modal
    public static <T> void openAdjustBillPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        System.out.println("openAdjustBillWindow-------" + productId);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<PurchaseReturnBillListDTO> observableLedgerList = FXCollections.observableArrayList();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
//        Button addButton = new Button("+ Add Product");
//        addButton.setMinWidth(130);
//        addButton.setMaxWidth(130);
//        addButton.setMinHeight(30);
//        addButton.setMaxHeight(30);
//        addButton.setId("submit-btn");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        // hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
        ToggleGroup tg = new ToggleGroup();
        RadioButton yes = new RadioButton();
        RadioButton no = new RadioButton();
        yes.setText("Yes");
        no.setText("NO");
        yes.setToggleGroup(tg);
        no.setToggleGroup(tg);

        HBox hbox2 = new HBox(yes, no);
        hbox2.setSpacing(7);
        hbox2.setPadding(new Insets(15, 0, 0, 10));
        hbox2.setAlignment(Pos.CENTER_LEFT);
        vbox_top.getChildren().addAll(hbox_top, hbox2);
        Object[] selectedItem = new Object[10];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<PurchaseReturnBillListDTO> tableView = new TableView();
        tableView.setVisible(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<PurchaseReturnBillListDTO, CheckBox> action = new TableColumn<>("Select");
        TableColumn<PurchaseReturnBillListDTO, String> debitNoteNo = new TableColumn<>("Debit Note No");
        TableColumn<PurchaseReturnBillListDTO, String> debitNoteDate = new TableColumn<>("Debit Note Date");
        TableColumn<PurchaseReturnBillListDTO, String> totalAmount = new TableColumn<>("Total amount");
        TableColumn<PurchaseReturnBillListDTO, String> paidAmt = new TableColumn<>("Paid Amt");
        TableColumn<PurchaseReturnBillListDTO, Void> remainingAmt = new TableColumn<>("Remaining Amt");

        // Adjusting the width for the Columns
        debitNoteNo.setPrefWidth(40);
        debitNoteDate.setPrefWidth(200);
        totalAmount.setPrefWidth(50);
        paidAmt.setPrefWidth(50);
        remainingAmt.setPrefWidth(50);

        tableView.getColumns().addAll(action, debitNoteNo, debitNoteDate, totalAmount, paidAmt, remainingAmt);

//        action.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        action.setCellValueFactory(new PropertyValueFactory<PurchaseReturnBillListDTO, CheckBox>("action"));
        // action.setCellFactory(column -> new CheckBoxTableCell<>());
        action.setEditable(true);
        //action.setCellFactory(CheckBoxTableCell.forTableColumn(action));
        debitNoteNo.setCellValueFactory(new PropertyValueFactory<>("debit_note_no"));
        debitNoteDate.setCellValueFactory(new PropertyValueFactory<>("debit_note_date"));
        totalAmount.setCellValueFactory(new PropertyValueFactory<>("total_amt"));
        paidAmt.setCellValueFactory(new PropertyValueFactory<>("total_amt"));
        remainingAmt.setCellValueFactory(new PropertyValueFactory<>("total_amt"));

        paidAmt.setCellFactory(TextFieldTableCellForSalesInvoiceWithConverter.forTableColumn());

//        action.setCellFactory(column -> new CheckBoxTableCell<>());


//        action.setCellValueFactory(cellData -> {
//            PurchaseReturnBillListDTO cellValue = cellData.getValue();
//            BooleanProperty property = cellValue.actionProperty();
//            ObservableList<PurchaseReturnBillListDTO> list = tableView.getItems();
//
//            for (PurchaseReturnBillListDTO object : list) {
//                if (object.getAction()) {
//                    System.out.println();
////                    Selected_index = Long.valueOf(object.getOrder_id());
//////                    System.out.println("Selected Index Pickup--> "+ Selected_index);
////                    handleAllCheckbox(Selected_index);
////                    String Selected_invoice = String.valueOf(object.getInvoice_no());
////                    Selected_status = String.valueOf(object.getOrder_status());
////                    lblAllNo.setText(Selected_invoice);
//                } else {
//                    tableView.getItems().clear();
//                //    calculateAllTotalAmount();
////                    lblAllNo.setText("");
//                }
//            }
//            return property;
//        });
//        action.setEditable(true);
//
//
//        action.setOnEditCommit(event -> {
//            System.out.println("hghdsgh");
//            // Here you can perform actions based on selection or deselection
//            PurchaseReturnBillListDTO item = event.getRowValue();
//            boolean newValue = event.getNewValue();
//            // Perform actions based on the newValue (selected or deselected)
//        });


        fetchReturnBillList();
        tableView.setItems(observableListPurchaseReturnList);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableListPurchaseReturnList);
        // focus to selected row index in productPopup
        if (!productId.isEmpty()) {
//            System.out.println("out index2------ " + index2);
            tableView.getSelectionModel().clearAndSelect(index2);

            Platform.runLater(() -> {
                // Scroll to the selected index after the TableView is fully rendered
                tableView.requestFocus();
                tableView.scrollTo(index2);
            });
        }


        yes.setOnAction(event -> {
            if (yes.isSelected()) {
                tableView.setVisible(true);
            } else {
                tableView.setVisible(false);
            }
        });


        no.setOnAction(event -> {
            if (no.isSelected()) {
                tableView.setVisible(false);
            }
        });


        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<PurchaseReturnBillListDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        PurchaseReturnBillListDTO productWindowDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        productIdSelected = productWindowDTO.getId();
                        selectedItem[0] = productWindowDTO.getDebit_note_no();
                        selectedItem[1] = productWindowDTO.getDebit_note_date();
                        selectedItem[2] = productWindowDTO.getId();
                        selectedItem[3] = productWindowDTO.getTotal_amt();


                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();

      /*  addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                selectedItem[1] = tfPower.getText();
                insertIntoTable(selectedItem[0].toString(), selectedItem[1].toString(), selectedItem[2].toString()
                        , selectedItem[3].toString());
            }
        });*/

// close Product popup on Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }


    //Get Product List in Product Modal
    public static void fetchReturnBillList() {
        observableListProduct.clear();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("sundry_creditor_id", "49");

            //To Send the Data in JSON Format
//            String formData = Globals.mapToString(body);
//            HttpResponse<String> response = APIClient.postJsonRequest(formData, EndPoints.GET_TRANX_PRODUCT_LIST);
            //To Send the Data in Form Format
            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_RETURN_BILL_LIST);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("list").getAsJsonArray();

                System.out.println("responseArr -> " + responseArray);
                if (responseArray.size() > 0) {
                    int count = 0;


                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
                        String index = String.valueOf(count);
                        String id = item.get("id").getAsString();
                        String totalAmt = item.get("Total_amt").getAsString();
                        String source = item.get("source").getAsString();
                        String invoice_id = item.get("invoice_id").getAsString();
                        // String barcode = item.get("barcode").getAsString();
                        String isSelected = "";
                        String debit_note_no = item.get("debit_note_no").getAsString();
                        String mrp = item.get("mrp") != null ? item.get("mrp").getAsString() : "0.00";
                        //  System.out.println("heeelo"+item.get("current_stock"));
                        String debit_note_date = item.get("debit_note_date").getAsString();
                        //Boolean action=false;

                        observableListPurchaseReturnList.add(new PurchaseReturnBillListDTO(new CheckBox(), totalAmt, debit_note_date, id, debit_note_no, invoice_id, isSelected, source));
                        count++;
                    }
                    //finding index of selected product
//                    if(!productId.isEmpty() ){
//                        System.out.println("productId >>>>>>>>>" + productId);
//                        for(TranxProductWindowDTO item : observableListProduct){
//                            System.out.println("item.getId() >>>>>>>>>" + item.getId());
//                            if (item.getId().equalsIgnoreCase(productId)) {
//                                System.out.println("now focus is ready in Product");
//                                index2 = observableListProduct.indexOf(item);
//                                System.out.println(".indexOf(item) " +  index2);
//                                System.out.println(" " + item);
//                            }
//                        }
//                    }


                } else {
                    System.out.println("responseObject is null");
                }
            } else {
                System.out.println("Error in response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static <T> void openInvoiceList(Long ledgerId, Stage stage, String title, Consumer<Object[]>
            callback) {
        System.out.println("Title in Ledger List:" + title);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(10, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedInvoice = new Object[2];
        Platform.runLater(() -> search.requestFocus());
        Integer initialIndex = -1;
        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxReturnsBillDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFocusTraversable(false);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxReturnsBillDTO, String> tcBillsrNo = new TableColumn<>("Sr.no");
        TableColumn<TranxReturnsBillDTO, String> tcBillNumber = new TableColumn<>("Invoice Number");
        TableColumn<TranxReturnsBillDTO, String> tcBillAmt = new TableColumn<>("Invoice Amount");
        TableColumn<TranxReturnsBillDTO, String> tcBillDate = new TableColumn<>("Invoice Date");
        tableView.getColumns().addAll(tcBillsrNo, tcBillNumber, tcBillAmt, tcBillDate);
        tcBillsrNo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        tcBillNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNo()));
        tcBillAmt.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceAmount()));
        tcBillDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceDate()));
        fetchLedgerBillList(ledgerId, "prtn");
        tableView.setItems(observableLedgerBillList);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableLedgerBillList);
        // Add a listener to the text property of the search TextField
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue
            tableView.setItems(observableLedgerBillList.filtered(item ->
                    item.getInvoiceNo().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getInvoiceAmount().contains(newValue)
            ));
        });

        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                }
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxReturnsBillDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxReturnsBillDTO tranxReturnsBillDTO = tableView.getSelectionModel().getSelectedItem();
                        selectedInvoice[0] = tranxReturnsBillDTO.getId();
                        selectedInvoice[1] = tranxReturnsBillDTO.getInvoiceNo();
                        callback.accept(selectedInvoice);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });

            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                TranxReturnsBillDTO tranxReturnsBillDTO = tableView.getSelectionModel().getSelectedItem();
                selectedInvoice[0] = tranxReturnsBillDTO.getId();
                selectedInvoice[1] = tranxReturnsBillDTO.getInvoiceNo();
                callback.accept(selectedInvoice);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 610);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }


    private static void fetchLedgerBillList(Long ledgerId, String key) {
        observableLedgerBillList.clear();
        APIClient apiClient = null;
        try {
            Map<String, String> body = new HashMap<>();
            if (key.equalsIgnoreCase("prtn"))
                body.put("sundry_creditor_id", "" + ledgerId);
            else body.put("sundry_debtors_id", "" + ledgerId);
            String formData = Globals.mapToStringforFormData(body);
            if (key.equalsIgnoreCase("prtn"))
                apiClient = new APIClient(EndPoints.GET_LEDGER_BILL_LIST, formData, RequestType.FORM_DATA);
            else
                apiClient = new APIClient(EndPoints.GET_DEBTOR_BILL_LIST, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    String responseBody = workerStateEvent.getSource().getValue().toString();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseArray = jsonObject.get("data").getAsJsonArray();
                        if (responseArray.size() > 0) {
                            for (JsonElement element : responseArray) {
                                JsonObject item = element.getAsJsonObject();
                                Long id = item.get("id").getAsLong();
                                String invoiceNo = item.get("invoice_no").getAsString();
                                String invoiceDate = item.get("invoice_date").getAsString();
                                Double invoiceAmt = item.get("total_amount").getAsDouble();
                                observableLedgerBillList.add(new TranxReturnsBillDTO("" + id, "", invoiceNo, "" + invoiceAmt,
                                        invoiceDate));
                            }
                        } else {
                            logger.debug("Response is Empty in  fetchLedgerBillList()");
                        }
                    } else {
                        logger.debug("Error in response fetchLedgerBillList()");
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Called setOnCancelled()----->" +
                            workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Called setOnFailed()----->" +
                            workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in fetchLedgerBillList()----->" + exceptionAsString);
        } finally {
            apiClient = null;
        }
    }

    public static <T> void openTranxRtnProductList(String invoiceId, Stage stage, String title, String key,
                                                   BiConsumer<ObservableList<TranxPurRowResEditDTO>, TranxPurInvoiceResEditDTO> callback) {
        List<Long> prIds = new ArrayList<>();
        ObservableList<TranxPurRowResEditDTO> mPurRow =
                FXCollections.observableArrayList();
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(10, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Platform.runLater(() -> search.requestFocus());
        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxRtnsBillProductListDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFocusTraversable(false);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxRtnsBillProductListDTO, CheckBox> tcSelect = new TableColumn<>("Select");
        TableColumn<TranxRtnsBillProductListDTO, String> tcInvoiceNo = new TableColumn<>("Invoice No");
        TableColumn<TranxRtnsBillProductListDTO, String> tcProductName = new TableColumn<>("Product Name");
        TableColumn<TranxRtnsBillProductListDTO, String> tcPackageName = new TableColumn<>("Package Name");
        TableColumn<TranxRtnsBillProductListDTO, String> tcUnitName = new TableColumn<>("Unit Name");
        TableColumn<TranxRtnsBillProductListDTO, String> tcBatchNo = new TableColumn<>("Batch No");
        TableColumn<TranxRtnsBillProductListDTO, String> tcExpDate = new TableColumn<>("Exp. Date");
        TableColumn<TranxRtnsBillProductListDTO, String> tcRtnQty = new TableColumn<>("Rtn.Qty");
        TableColumn<TranxRtnsBillProductListDTO, String> tcQty = new TableColumn<>("Qty");
        TableColumn<TranxRtnsBillProductListDTO, String> tcFreeQty = new TableColumn<>("Free Qty");
        TableColumn<TranxRtnsBillProductListDTO, String> tcRate = new TableColumn<>("Rate");
        tableView.getColumns().addAll(tcSelect, tcInvoiceNo, tcProductName, tcPackageName, tcUnitName, tcBatchNo, tcExpDate,
                tcRtnQty, tcQty, tcFreeQty, tcRate);

        tcSelect.setCellValueFactory(new PropertyValueFactory<TranxRtnsBillProductListDTO, CheckBox>("isSelect"));
        tcInvoiceNo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNo()));
        tcProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        tcPackageName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPackageName()));
        tcUnitName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnitName()));
        tcBatchNo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBatchNo()));
        tcExpDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpDate()));
        tcRtnQty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRtnQty()));
        tcQty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQty()));
        tcFreeQty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFreeQty()));
        tcRate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRate()));
        if (key.equalsIgnoreCase("prtn"))
            fetchRtnProductList(invoiceId);
        else
            fetchRtnProductListSalesRtn(invoiceId);
        tableView.setItems(observableTranxRtnProductList);
        vBox.getChildren().addAll(tableView);
        // Add a listener to the text property of the search TextField
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue
            tableView.setItems(observableTranxRtnProductList.filtered(item ->
                    item.getInvoiceNo().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getInvoiceNo().contains(newValue) ||
                            item.getProductName().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getPackageName().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getUnitName().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getBatchNo().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getRtnQty().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getQty().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getFreeQty().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getRate().toLowerCase().contains(newValue.toLowerCase())));
        });
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                }
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox v_box = new HBox();
        Button submit = new Button("Submit");
        v_box.getChildren().add(spacer1);
        v_box.getChildren().add(submit);
        final String IDLE_BUTTON_STYLE = "-fx-padding: 5px 14px;-fx-font-family: \"Inter\"; sans-serif;" +
                "-fx-pref-width: 90px;-fx-pref-height: 34px;-fx-margin-left: 10px;-fx-font-size: 14px;" +
                "-fx-text-fill: white;-fx-background-color: #21c78a; -fx-font-weight: bold;-fx-background-radius: 4px;" +
                "-fx-border-radius: 4px;";
        final String HOVERED_BUTTON_STYLE = "-fx-padding: 5px 14px;-fx-font-family: \"Inter\", sans-serif;" +
                "-fx-pref-width: 90px;-fx-pref-height: 34px;-fx-margin-left: 10px;-fx-font-size: 14px;" +
                "-fx-text-fill: white;-fx-border-color: #20a574; -fx-background-color: #20a574;-fx-cursor: hand;" +
                "-fx-font-weight: bold;-fx-background-radius: 4px;" +
                "-fx-border-radius: 4px;";
        submit.setStyle(IDLE_BUTTON_STYLE);
        submit.setOnMouseExited(e -> submit.setStyle(IDLE_BUTTON_STYLE));
        submit.setOnMouseEntered(e -> submit.setStyle(HOVERED_BUTTON_STYLE));
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TranxRtnsBillProductListDTO mList : observableTranxRtnProductList) {
                    if (mList.getIsSelect().isSelected()) {
                        prIds.add(mList.getPrId());
                    }
                }
                for (Long pr1 : prIds) {
                    // For each Times, we want to retrieve the corresponding Product Id from the productRowList list. We'll use the
                    // Java 8 Streams API to do this
                    productRowList.stream()
                            // Check if the productRowList list contains a Product with this ID
                            .filter(p -> p.getProductId().longValue() == pr1.longValue())
                            .findFirst()
                            // Add the new TranxRtnsBillProductListDTO to the list
                            .ifPresent(s -> {
                                mPurRow.add(s);
                            });
                }
                callback.accept(mPurRow, invoiceResEditDTO);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        submit.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                for (TranxRtnsBillProductListDTO mList : observableTranxRtnProductList) {
                    if (mList.getIsSelect().isSelected()) {
                        prIds.add(mList.getPrId());
                    }
                }
                for (Long pr1 : prIds) {
                    // For each Times, we want to retrieve the corresponding Product Id from the productRowList list. We'll use the
                    // Java 8 Streams API to do this
                    productRowList.stream()
                            // Check if the productRowList list contains a Product with this ID
                            .filter(p -> p.getProductId().longValue() == pr1.longValue())
                            .findFirst()
                            // Add the new TranxRtnsBillProductListDTO to the list
                            .ifPresent(s -> {
                                mPurRow.add(s);
                            });
                }
                callback.accept(mPurRow, invoiceResEditDTO);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }

        });

        v_box.setPadding(new Insets(10, 10, 10, 0));
        borderPane.setBottom(v_box);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxRtnsBillProductListDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxRtnsBillProductListDTO tranxReturnsBillDTO = tableView.getSelectionModel().getSelectedItem();

                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 680);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {

                if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("modify")) {
                    targetButton.getText();
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("submit")) {
                    targetButton.getText();
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("cancel")) {
                    targetButton.getText();
                } else {
                    KeyEvent newEvent = new KeyEvent(
                            null,
                            null,
                            KeyEvent.KEY_PRESSED,
                            "",
                            "\t",
                            KeyCode.TAB,
                            event.isShiftDown(),
                            event.isControlDown(),
                            event.isAltDown(),
                            event.isMetaDown()
                    );
                    Event.fireEvent(event.getTarget(), newEvent);
                    event.consume();
                }
            }
        });
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }

    private static void fetchRtnProductList(String invoiceId) {
        observableTranxRtnProductList.clear();
        productRowList.clear();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("id", invoiceId); // Assuming search is not dependent on ledger type
            //To Send the Data in Form Format
            String formData = Globals.mapToStringforFormData(body);
//            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_PUR_INVOICE_BY_ID);
            APIClient apiClient = new APIClient(EndPoints.GET_PUR_INVOICE_BY_ID, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    String responseBody = workerStateEvent.getSource().getValue().toString();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        String invoiceNo = jsonObject.get("invoice_data").getAsJsonObject().get("invoice_no").getAsString();
                        Long id = jsonObject.get("invoice_data").getAsJsonObject().get("id").getAsLong();
                        invoiceResEditDTO = new TranxPurInvoiceResEditDTO();
                        invoiceResEditDTO.setId(id);
                        invoiceResEditDTO.setInvoiceNo(invoiceNo);
                        invoiceResEditDTO.setDiscountLedgerId(jsonObject.get("discountLedgerId").getAsLong());
                        invoiceResEditDTO.setDiscountInPer(jsonObject.get("discountInPer").getAsDouble());
                        invoiceResEditDTO.setDiscountInAmt(jsonObject.get("discountInAmt").getAsDouble());
                        invoiceResEditDTO.setGrossTotal(jsonObject.get("grossTotal").getAsDouble());
                        invoiceResEditDTO.setNarration(jsonObject.get("narration").getAsString());
                        invoiceResEditDTO.setPaymentMode(jsonObject.get("paymentMode").getAsString());
                        invoiceResEditDTO.setTcs_amt(jsonObject.get("tcs_amt").getAsDouble());
                        invoiceResEditDTO.setTcs_per(jsonObject.get("tcs_per").getAsDouble());
                        invoiceResEditDTO.setTcs_mode(jsonObject.get("tcs_mode").getAsString());
                        invoiceResEditDTO.setTotalAmount(jsonObject.get("totalAmount").getAsDouble());
                        invoiceResEditDTO.setTotalFreeQty(jsonObject.get("totalFreeQty").getAsDouble());
                        invoiceResEditDTO.setTotalPurchaseDiscountAmt(jsonObject.get("totalPurchaseDiscountAmt").getAsDouble());
                        invoiceResEditDTO.setTotalQty(jsonObject.get("totalQty").getAsDouble());
                        invoiceResEditDTO.setTotalTax(jsonObject.get("totalTax").getAsDouble());
                        JsonObject invoiceData = jsonObject.get("invoice_data").getAsJsonObject();
                        invoiceResEditDTO.setAdditionalTotalCharges(invoiceData.get("additional_charges_total").getAsDouble());
                        invoiceResEditDTO.setInvoiceDate(invoiceData.get("invoice_dt").getAsString());
                        if (invoiceData.has("isRoundOffCheck") && !invoiceData.get("isRoundOffCheck").isJsonNull())
                            invoiceResEditDTO.setIsroundOff(invoiceData.get("isRoundOffCheck").getAsBoolean());
                        else
                            invoiceResEditDTO.setIsroundOff(false);
                        invoiceResEditDTO.setPurchaseAcctId(invoiceData.get("purchase_account_ledger_id").getAsLong());
                        invoiceResEditDTO.setPurchaseSrNo(invoiceData.get("purchase_sr_no").getAsLong());
                        invoiceResEditDTO.setRoundoffAmt(invoiceData.get("roundoff").getAsDouble());
                        invoiceResEditDTO.setSupplierId(invoiceData.get("supplierId").getAsLong());
                        invoiceResEditDTO.setTransactionDate(invoiceData.get("transaction_dt").getAsString());
                        invoiceResEditDTO.setTranxUniqueCode(invoiceData.get("tranx_unique_code").getAsString());
                        JsonArray rowData = jsonObject.get("row").getAsJsonArray();
                        if (rowData.size() > 0) {
                            for (JsonElement element : rowData) {
                                JsonObject rowObject = element.getAsJsonObject();
                                TranxPurRowResEditDTO tRowData = null;
                                try {
                                    tRowData = new Gson().fromJson(rowObject, TranxPurRowResEditDTO.class);
                                    productRowList.add(tRowData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String productName = rowObject.get("product_name").getAsString();
                                String pkgName = rowObject.get("packing").getAsString();
                                String unitName = rowObject.get("unit_name").getAsString();
                                String batchNo = rowObject.get("batch_no").getAsString();
                                String expDate = rowObject.get("b_expiry").getAsString();
                                String rtnQty = rowObject.get("returnable_qty").getAsString();
                                String qty = rowObject.get("qty").getAsString();
                                String freeQty = rowObject.get("free_qty").getAsString();
                                String rate = rowObject.get("rate").getAsString();
                                Long prId = rowObject.get("product_id").getAsLong();
                                Boolean isBatch = rowObject.get("is_batch").getAsBoolean();
                                TranxRtnsBillProductListDTO prList = new TranxRtnsBillProductListDTO(new CheckBox(), "" + id,
                                        invoiceNo, productName, pkgName, unitName, batchNo, expDate, rtnQty, qty, freeQty,
                                        rate, prId, isBatch);
                                observableTranxRtnProductList.add(prList);
                            }

                        } else {
                            logger.debug("Response is Empty fetchRtnProductList()");
                        }
                    } else {
                        logger.debug("Error in response fetchRtnProductList()");
                    }
                }
            });
            apiClient.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in fetchRtnProductList()--->" + exceptionAsString);
            e.printStackTrace();
        }

    }

    private static void fetchRtnProductListSalesRtn(String invoiceId) {
        observableTranxRtnProductList.clear();
        productRowList.clear();
        try {
            Map<String, String> body = new HashMap<>();
            body.put("id", invoiceId); // Assuming search is not dependent on ledger type
            //To Send the Data in Form Format
            String formData = Globals.mapToStringforFormData(body);
            APIClient apiClient = new APIClient(EndPoints.TRANX_SALES_INVOICE_EDIT_BY_ID, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    String responseBody = workerStateEvent.getSource().getValue().toString();
                    JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        String invoiceNo = jsonObject.get("invoice_data").getAsJsonObject().get("invoice_no").getAsString();
                        Long id = jsonObject.get("invoice_data").getAsJsonObject().get("id").getAsLong();
                        invoiceResEditDTOSalesRtn = new TranxSalesInvoiceResEditDTO();
                        invoiceResEditDTOSalesRtn.setId(id);
                        invoiceResEditDTOSalesRtn.setInvoiceNo(invoiceNo);
                        invoiceResEditDTOSalesRtn.setDiscountLedgerId(jsonObject.get("discountLedgerId").getAsLong());
                        invoiceResEditDTOSalesRtn.setDiscountInPer(jsonObject.get("discountInPer").getAsDouble());
                        invoiceResEditDTOSalesRtn.setDiscountInAmt(jsonObject.get("discountInAmt").getAsDouble());
                        invoiceResEditDTOSalesRtn.setGrossTotal(jsonObject.get("grossTotal").getAsDouble());
                        invoiceResEditDTOSalesRtn.setNarration(jsonObject.get("narration").getAsString());
                       // invoiceResEditDTOSalesRtn.setPaymentMode(jsonObject.get("paymentMode").getAsString());
                        invoiceResEditDTOSalesRtn.setTcs_amt(jsonObject.get("tcs_amt").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTcs_per(jsonObject.get("tcs_per").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTcs_mode(jsonObject.get("tcs_mode").getAsString());
                        invoiceResEditDTOSalesRtn.setTotalAmount(jsonObject.get("totalamt").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTotalFreeQty(jsonObject.get("totalFreeQty").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTotalSalesDiscountAmt(jsonObject.get("totalSalesDiscountAmt").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTotalQty(jsonObject.get("totalQty").getAsDouble());
                        invoiceResEditDTOSalesRtn.setTotalTax(jsonObject.get("totalTax").getAsDouble());
                        JsonObject invoiceData = jsonObject.get("invoice_data").getAsJsonObject();
                        invoiceResEditDTOSalesRtn.setAdditionalTotalCharges(invoiceData.get("additional_charges_total").getAsDouble());
                        invoiceResEditDTOSalesRtn.setInvoiceDate(invoiceData.get("invoice_dt").getAsString());
                        if (invoiceData.has("isRoundOffCheck") && !invoiceData.get("isRoundOffCheck").isJsonNull())
                            invoiceResEditDTOSalesRtn.setIsroundOff(invoiceData.get("isRoundOffCheck").getAsBoolean());
                        else
                            invoiceResEditDTOSalesRtn.setIsroundOff(false);
                        invoiceResEditDTOSalesRtn.setSalesAcctId(invoiceData.get("sales_account_ledger_id").getAsLong());
                        invoiceResEditDTOSalesRtn.setSalesSrNo(invoiceData.get("sales_sr_no").getAsLong());
                        invoiceResEditDTOSalesRtn.setRoundoffAmt(invoiceData.get("roundoff").getAsDouble());
                        invoiceResEditDTOSalesRtn.setSupplierId(invoiceData.get("supplierId").getAsLong());
                        invoiceResEditDTOSalesRtn.setTransactionDate(invoiceData.get("transaction_dt").getAsString());
                        invoiceResEditDTOSalesRtn.setTranxUniqueCode(invoiceData.get("tranx_unique_code").getAsString());
                        JsonArray rowData = jsonObject.get("row").getAsJsonArray();
                        if (rowData.size() > 0) {
                            for (JsonElement element : rowData) {
                                JsonObject rowObject = element.getAsJsonObject();
                                TranxPurRowResEditDTO tRowData = null;
                                try {
                                    tRowData = new Gson().fromJson(rowObject, TranxPurRowResEditDTO.class);
                                    productRowList.add(tRowData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String productName = rowObject.get("product_name").getAsString();
                                String pkgName = rowObject.get("packing").getAsString();
                                String unitName = rowObject.get("unit_name").getAsString();
                                String batchNo = rowObject.get("batch_no").getAsString();
                                String expDate = rowObject.get("b_expiry").getAsString();
                                String rtnQty = rowObject.get("returnable_qty").getAsString();
                                String qty = rowObject.get("qty").getAsString();
                                String freeQty = rowObject.get("free_qty").getAsString();
                                String rate = rowObject.get("rate").getAsString();
                                Long prId = rowObject.get("product_id").getAsLong();
                                Boolean isBatch = rowObject.get("is_batch").getAsBoolean();
                                TranxRtnsBillProductListDTO prList = new TranxRtnsBillProductListDTO(new CheckBox(), "" + id,
                                        invoiceNo, productName, pkgName, unitName, batchNo, expDate, rtnQty, qty, freeQty,
                                        rate, prId, isBatch);
                                observableTranxRtnProductList.add(prList);
                            }

                        } else {
                            logger.debug("Response is Empty fetchRtnProductList()");
                        }
                    } else {
                        logger.debug("Error in response fetchRtnProductList()");
                    }
                }
            });
            apiClient.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in fetchRtnProductList() in Sales Return--->" + exceptionAsString);
            e.printStackTrace();
        }

    }


    //Patient Popup
    public static <T> void openPatientPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
//        SwitchButton switchButton = new SwitchButton();
//        Label titleLabel = new Label("Patient");
        // Set the font size
//        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
//        hbox_top.getChildren().add(switchButton);
//        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
//        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setMinHeight(0);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
//        HBox.setMargin(search, new Insets(0, 0, 0, 10));
//        hbox_top1.getChildren().addAll(search);
//        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[5];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<PatientDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<PatientDTO, String> patientCode = new TableColumn<>("Code");
        TableColumn<PatientDTO, String> patientName = new TableColumn<>("Patient Name");
        TableColumn<PatientDTO, String> mobileNo = new TableColumn<>("Mobile No");
        TableColumn<PatientDTO, String> patientAddress = new TableColumn<>("Address");


        tableView.getColumns().addAll(patientCode, patientName, mobileNo, patientAddress);

        patientCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        patientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        mobileNo.setCellValueFactory(new PropertyValueFactory<>("mobileNo"));
        patientAddress.setCellValueFactory(new PropertyValueFactory<>("patientAddress"));

       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        getPatient();
        tableView.setItems(observableListPatient);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
//        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                getPatient("", "SD"); // Fetch only SD type ledgers
//            } else {
//                getPatient("", ""); // Fetch all ledgers
//            }
//        });
        tableView.setItems(observableListPatient);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListPatient.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            getPatient(newValue,"");
        });*/
//        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent keyEvent) {
//                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
//                if (searchKey.length() >= 3) {
//                    getPatient(searchKey, "");
//                } else if (searchKey.isEmpty()) {
//                    getPatient("", "");
//                }
//                tableView.setItems(observableListPatient);
//            }
//        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<PatientDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        PatientDTO patientDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = patientDTO.getPatientCode();
                        selectedItem[1] = patientDTO.getPatientName();
                        selectedItem[2] = patientDTO.getPatientAddress();
                        selectedItem[3] = patientDTO.getMobileNo();
                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }


    //Get patient Data
    private static void getPatient() {
        observableListPatient.clear();
        try {
            //tblvCounterSalePatientMaster.getItems().clear();
            HttpResponse<String> response = APIClient.getRequest(PATIENT_LIST_ENDPOINT);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println(jsonObject);
//            ObservableList<PatientDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("responseObject");

                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String patientName = item.get("patientName").getAsString();

                        String age = item.get("age").getAsString();
                        String patientAddress = item.get("patientAddress").getAsString();
                        String mobileNumber = item.get("mobileNumber").getAsString();
                        String birthDate = item.get("birthDate").getAsString();
                        String idNo = item.get("idNo").getAsString();
                        String gender = item.get("gender").getAsString();
                        String bloodGroup = item.get("bloodGroup").getAsString();
                        String patientCode = item.get("patientCode").getAsString();
                        String pincode = item.get("pincode").getAsString();
                        String weight = item.get("weight").getAsString();
                        String tbDiagnosisDate = item.get("TbDiagnosisDate").getAsString();
                        String tbTreatmentInitiationDate = item.get("TbTreatmentInitiationDate").getAsString();


                        observableListPatient.add(new PatientDTO(id, patientName, patientCode,
                                patientAddress, mobileNumber, pincode, gender, age, birthDate, idNo, bloodGroup, weight, tbDiagnosisDate, tbTreatmentInitiationDate)
                        );
                    }
//                    tblcCounterSalePatMasPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
//                    tblcCounterSalePatMasCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
//                    tblcCounterSalePatMasAddress.setCellValueFactory(new PropertyValueFactory<>("patientAddress"));
//                    tblcCounterSalePatMasMobileNo.setCellValueFactory(new PropertyValueFactory<>("mobileNo"));
//
//
//                    tblvCounterSalePatientMaster.setItems(observableList);

                } else {
                    System.out.println("responseObject is null");
                }
            } else {
                System.out.println("Error in response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Doctor Name Pop Up
    public static <T> void openDoctorPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
//        SwitchButton switchButton = new SwitchButton();
//        Label titleLabel = new Label("Patient");
        // Set the font size
//        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
//        hbox_top.getChildren().add(switchButton);
//        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
//        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setMinHeight(0);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
//        HBox.setMargin(search, new Insets(0, 0, 0, 10));
//        hbox_top1.getChildren().addAll(search);
//        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[5];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<DoctorMasterDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<DoctorMasterDTO, String> doctorName = new TableColumn<>("Doctor Name");


        tableView.getColumns().addAll(doctorName);

        doctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));

       /* cmActions.setCellFactory(param -> {
            final TableCell<ProductContentsMasterDTO, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button delButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {

                    delButton.setOnAction(event -> {
                        contentEdtIdx = -1;
                        tbllstContentMasterInfo.remove(getIndex());

                    });
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        contentEdtIdx = getIndex();
                        ProductContentsMasterDTO fnContentsDetails = tbllstContentMasterInfo.get(contentEdtIdx);
                        if (fnContentsDetails != null) {
                            setComboBoxValue(cbContentName, fnContentsDetails.getContentName());
                            tfPower.setText(fnContentsDetails.getPower());
                            setComboBoxValue(cbPackage, fnContentsDetails.getPacking());
                            setComboBoxValue(cbContentType, fnContentsDetails.getContentType());
                        }
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(delButton);
                    hbActions.setSpacing(10.0);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(hbActions);
                    }
                }

            };
            return cell;
        });*/
        getDoctorMaster();
        tableView.setItems(observableListDoctor);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
//        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                getPatient("", "SD"); // Fetch only SD type ledgers
//            } else {
//                getPatient("", ""); // Fetch all ledgers
//            }
//        });
        tableView.setItems(observableListDoctor);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListPatient.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            getPatient(newValue,"");
        });*/
//        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent keyEvent) {
//                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
//                if (searchKey.length() >= 3) {
//                    getPatient(searchKey, "");
//                } else if (searchKey.isEmpty()) {
//                    getPatient("", "");
//                }
//                tableView.setItems(observableListPatient);
//            }
//        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<DoctorMasterDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        DoctorMasterDTO doctorMasterDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = doctorMasterDTO.getDoctorName();
                        selectedItem[1] = doctorMasterDTO.getId();

                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }

    //Get Doctor Master Data
    private static void getDoctorMaster() {
        try {

            HttpResponse<String> response = APIClient.getRequest("get_all_doctor_master");
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println(jsonObject);
//            ObservableList<DoctorMasterDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("responseObject");

                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();


                        int id = item.get("id").getAsInt();
                        String doctorName = item.get("doctorName").getAsString();
                        String hospitalName = item.get("hospitalName").getAsString();
                        String specialization = item.get("specialization").getAsString();
                        String hospitalAddress = item.get("hospitalAddress").getAsString();
                        String mobileNumber = item.get("mobileNumber").getAsString();

                        observableListDoctor.add(new DoctorMasterDTO(id, doctorName, hospitalName, "", hospitalAddress, mobileNumber, "", "", specialization));

                    }

//                    tblcCounterSaleDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
//
//                    tblvCounterSaleDoctor.setItems(observableList);

                } else {
                    System.out.println("responseObject is null");
                }
            } else {
                System.out.println("Error in response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//Batchpop up open

    public static <T> void openBatchPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(1200);
        hbox_top.setMaxWidth(1200);
        hbox_top.setPrefWidth(1200);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
//        SwitchButton switchButton = new SwitchButton();
//        Label titleLabel = new Label("Patient");
        // Set the font size
//        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
//        hbox_top.getChildren().add(switchButton);
//        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
//        Button addButton = new Button("+ Add");
//        addButton.setMinWidth(120);
//        addButton.setMaxWidth(120);
//        addButton.setMinHeight(30);
//        addButton.setMaxHeight(30);
//        addButton.setId("submit-btn");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
//        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        // hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(1200);
        hbox_top1.setMaxWidth(1200);
        hbox_top1.setPrefWidth(1200);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setMinHeight(0);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
//        HBox.setMargin(search, new Insets(0, 0, 0, 10));
//        hbox_top1.getChildren().addAll(search);
//        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[19];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxBatchWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxBatchWindowDTO, String> b_no = new TableColumn<>("Batch ");
        TableColumn<TranxBatchWindowDTO, String> pur_date = new TableColumn<>("Pur Date");
        TableColumn<TranxBatchWindowDTO, String> manufacturing_date = new TableColumn<>("Mfg.Date");
        TableColumn<TranxBatchWindowDTO, String> b_expiry = new TableColumn<>("Exp Date");
        TableColumn<TranxBatchWindowDTO, String> mrp = new TableColumn<>("MRP");
        TableColumn<TranxBatchWindowDTO, String> purRate = new TableColumn<>("Pur Rate");
        TableColumn<TranxBatchWindowDTO, String> b_qty = new TableColumn<>("Qty");
        TableColumn<TranxBatchWindowDTO, String> b_freeQty = new TableColumn<>("Free Qty");
        TableColumn<TranxBatchWindowDTO, String> b_dis_per = new TableColumn<>("Disc %");
        TableColumn<TranxBatchWindowDTO, String> b_dis_amt = new TableColumn<>("Disc Amt");
        TableColumn<TranxBatchWindowDTO, String> barcode = new TableColumn<>("Barcode");
        TableColumn<TranxBatchWindowDTO, String> margin = new TableColumn<>("Margin %");
        TableColumn<TranxBatchWindowDTO, String> b_rate_a = new TableColumn<>("FSR");
        TableColumn<TranxBatchWindowDTO, String> b_rate_b = new TableColumn<>("CSR");
        TableColumn<TranxBatchWindowDTO, String> b_rate_c = new TableColumn<>("Sale Rate");
//        TableColumn<TranxBatchWindowDTO, String> action = new TableColumn<>("Action");


        tableView.getColumns().addAll(b_no, pur_date, manufacturing_date, b_expiry, mrp, purRate, b_qty, b_freeQty, b_dis_per, b_dis_amt, barcode, margin, b_rate_a, b_rate_b, b_rate_c);

        b_no.setCellValueFactory(new PropertyValueFactory<>("b_no"));
        pur_date.setCellValueFactory(new PropertyValueFactory<>("pur_date"));
        manufacturing_date.setCellValueFactory(new PropertyValueFactory<>("manufacturing_date"));
        b_expiry.setCellValueFactory(new PropertyValueFactory<>("b_expiry"));
        mrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        purRate.setCellValueFactory(new PropertyValueFactory<>("PurRate"));
        b_qty.setCellValueFactory(new PropertyValueFactory<>("b_qty"));
        b_freeQty.setCellValueFactory(new PropertyValueFactory<>("b_freeQty"));
        b_dis_per.setCellValueFactory(new PropertyValueFactory<>("b_dis_per"));
        b_dis_amt.setCellValueFactory(new PropertyValueFactory<>("b_dis_amt"));
        barcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        margin.setCellValueFactory(new PropertyValueFactory<>("margin"));
        b_rate_a.setCellValueFactory(new PropertyValueFactory<>("b_rate_a"));
        b_rate_b.setCellValueFactory(new PropertyValueFactory<>("b_rate_b"));
        b_rate_c.setCellValueFactory(new PropertyValueFactory<>("b_rate_c"));
//
        b_no.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        pur_date.prefWidthProperty().bind(tableView.widthProperty().multiply(0.07));
        manufacturing_date.prefWidthProperty().bind(tableView.widthProperty().multiply(0.07));
        b_expiry.prefWidthProperty().bind(tableView.widthProperty().multiply(0.07));
        mrp.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
        purRate.prefWidthProperty().bind(tableView.widthProperty().multiply(0.08));
        b_qty.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
        b_freeQty.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        b_dis_per.prefWidthProperty().bind(tableView.widthProperty().multiply(0.05));
        b_dis_amt.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        barcode.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        margin.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        b_rate_a.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        b_rate_b.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));
        b_rate_c.prefWidthProperty().bind(tableView.widthProperty().multiply(0.06));


        fetchDataOfProductAllBatches();
        tableView.setItems(observableListBatch);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
//        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                getPatient("", "SD"); // Fetch only SD type ledgers
//            } else {
//                getPatient("", ""); // Fetch all ledgers
//            }
//        });
        tableView.setItems(observableListBatch);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListPatient.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            getPatient(newValue,"");
        });*/
//        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent keyEvent) {
//                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
//                if (searchKey.length() >= 3) {
//                    getPatient(searchKey, "");
//                } else if (searchKey.isEmpty()) {
//                    getPatient("", "");
//                }
//                tableView.setItems(observableListPatient);
//            }
//        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxBatchWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxBatchWindowDTO batchWindowDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = batchWindowDTO.getB_no();
                        selectedItem[1] = batchWindowDTO.getPur_date();
                        selectedItem[2] = batchWindowDTO.getManufacturing_date();
                        selectedItem[3] = batchWindowDTO.getB_expiry();
                        selectedItem[4] = batchWindowDTO.getMrp();
                        selectedItem[5] = batchWindowDTO.getPurRate();
                        selectedItem[6] = batchWindowDTO.getB_qty();
                        selectedItem[7] = batchWindowDTO.getB_freeQty();
                        selectedItem[8] = batchWindowDTO.getB_dis_per();
                        selectedItem[9] = batchWindowDTO.getB_dis_amt();
                        selectedItem[10] = batchWindowDTO.getBarcode();
                        selectedItem[11] = batchWindowDTO.getMargin();
                        selectedItem[12] = batchWindowDTO.getB_rate_a();
                        selectedItem[13] = batchWindowDTO.getB_rate_b();
                        selectedItem[14] = batchWindowDTO.getB_rate_c();
                        selectedItem[15] = batchWindowDTO.getCosting();
                        selectedItem[16] = batchWindowDTO.getCostingWithTax();
                        selectedItem[17] = batchWindowDTO.getB_details_id();
                        selectedItem[18] = batchWindowDTO.getProduct_id();


                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });

        tableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!tableView.getSelectionModel().isEmpty()) {
                    TranxBatchWindowDTO batchWindowDTO = (TranxBatchWindowDTO) tableView.getSelectionModel().getSelectedItem();
                    selectedItem[0] = batchWindowDTO.getB_no();
                    selectedItem[1] = batchWindowDTO.getPur_date();
                    selectedItem[2] = batchWindowDTO.getManufacturing_date();
                    selectedItem[3] = batchWindowDTO.getB_expiry();
                    selectedItem[4] = batchWindowDTO.getMrp();
                    selectedItem[5] = batchWindowDTO.getPurRate();
                    selectedItem[6] = batchWindowDTO.getB_qty();
                    selectedItem[7] = batchWindowDTO.getB_freeQty();
                    selectedItem[8] = batchWindowDTO.getB_dis_per();
                    selectedItem[9] = batchWindowDTO.getB_dis_amt();
                    selectedItem[10] = batchWindowDTO.getBarcode();
                    selectedItem[11] = batchWindowDTO.getMargin();
                    selectedItem[12] = batchWindowDTO.getB_rate_a();
                    selectedItem[13] = batchWindowDTO.getB_rate_b();
                    selectedItem[14] = batchWindowDTO.getB_rate_c();
                    selectedItem[15] = batchWindowDTO.getCosting();
                    selectedItem[16] = batchWindowDTO.getCostingWithTax();
                    selectedItem[17] = batchWindowDTO.getB_details_id();
                    selectedItem[18] = batchWindowDTO.getProduct_id();


                    callback.accept(selectedItem);
                    primaryStage.close();
                    OverlaysEffect.removeOverlaysEffect(stage);
                    event.consume();
                }
            }
        });

        Scene scene = new Scene(borderPane, 1200, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
//        addButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
////                primaryStage.close();
//                OverlaysEffect.removeOverlaysEffect(stage);
//            }
//        });
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Platform.runLater(() -> {
            tableView.requestFocus();
            tableView.getSelectionModel().select(0);
            tableView.getFocusModel().focus(0);
        });
    }

    private static <
            T> ComboBox<T> createComboBox(String promptText, EventHandler<ActionEvent> actionHandler) {
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(200);
        comboBox.setMaxWidth(200);
        comboBox.setMinWidth(200);
        comboBox.setMaxHeight(32);
        comboBox.setMinHeight(32);
        comboBox.setPrefHeight(32);
        comboBox.setPromptText(promptText);
        comboBox.setOnAction(actionHandler);
        return comboBox;
    }

    public static List<CommonDTO> getIndianStateData() {
        try {
            HttpResponse<String> response = APIClient.getRequest("getIndianState");
            System.out.println("getIndianStateData =>" + response);
            AreaHeadIndianStateResDTO areaHeadIndianStateResDTO = new Gson().fromJson(response.body(), AreaHeadIndianStateResDTO.class);

            if (areaHeadIndianStateResDTO.getResponseStatus() == 200) {
                List<AreaHeadIndianStateDTO> list1 = areaHeadIndianStateResDTO.getResponseObject();
                for (AreaHeadIndianStateDTO contentList : list1) {
                    contentNamesList.add(new CommonDTO(contentList.getStateName(), contentList.getId().toString()));
                }
            } else {
                logger.info("ResponseObject is null--->getIndianStateData()");
            }

        } catch (Exception e) {
            logger.error("Exception in getIndianStateData()");
        }

        return contentNamesList;
    }


    public static List<CommonDTO> getAllZonesData() {
        try {
            HttpResponse<String> response = APIClient.getRequest(EndPoints.GET_ALL_ZONES);
            System.out.println("getIndianStateData =>" + response);
            AreaHeadZoneResDTO areaHeadZoneResDTO = new Gson().fromJson(response.body(), AreaHeadZoneResDTO.class);

            if (areaHeadZoneResDTO.getResponseStatus() == 200) {
                List<AreaHeadZoneDTO> list1 = areaHeadZoneResDTO.getResponseObject();
                for (AreaHeadZoneDTO contentList : list1) {
                    contentNamesList1.add(new CommonDTO(contentList.getZoneName(), contentList.getId().toString()));
                }
            } else {
                logger.info("ResponseObject is null--->getIndianStateData()");
            }

        } catch (Exception e) {
            logger.error("Exception in getIndianStateData()");
        }

        return contentNamesList1;
    }

    public static List<CommonDTO> getAllRegions() {
        try {
            HttpResponse<String> response = APIClient.getRequest(EndPoints.GET_ALL_REGIONS);
            System.out.println("getContentNames =>" + response);
            AreaHeadRegionsResDTO areaHeadRegionsResDTO = new Gson().fromJson(response.body(), AreaHeadRegionsResDTO.class);

            if (areaHeadRegionsResDTO.getResponseStatus() == 200) {
                List<AreaHeadRegionDTO> list1 = areaHeadRegionsResDTO.getResponseObject();
                for (AreaHeadRegionDTO contentList : list1) {
                    contentNamesList2.add(new CommonDTO(contentList.getRegionName(), contentList.getId().toString()));
                }
            } else {
                logger.info("ResponseObject is null--->getContentNames()");
            }

        } catch (Exception e) {
            logger.error("Exception in getIndianStateData()");
        }

        return contentNamesList2;
    }


    private static Button createButtonWithImage() {

        ImageView imageView = new ImageView(new Image(GenivisApplication.class.getResourceAsStream("/com/opethic/genivis/ui/assets/add3.png")));
        imageView.setFitWidth(15);
        imageView.setFitHeight(15);
        Button button = new Button();
        button.setMaxWidth(25);
        button.setMinWidth(25);
        button.setPrefWidth(25);
        button.setMaxHeight(30);
        button.setPrefHeight(30);
        button.setMinHeight(30);
        button.setGraphic(imageView);
        button.getStyleClass().add("add-button-style");

        return button;
    }

    public static <T> void openAreaHeadPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();

        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(998);
        hbox_top.setMaxWidth(998);
        hbox_top.setPrefWidth(998);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label("Add New " + title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });

        HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_center = new HBox();
        hbox_center.setMinWidth(998);
        hbox_center.setMaxWidth(998);
        hbox_center.setPrefWidth(998);
        hbox_center.setMaxHeight(50);
        hbox_center.setMinHeight(50);
        hbox_center.setMaxHeight(50);
        hbox_center.setAlignment(Pos.CENTER);
        hbox_center.setStyle("-fx-background-color: white;");
        hbox_center.setSpacing(10); // Set spacing between labels
        hbox_center.setPadding(new Insets(10, 0, 0, 0)); // Set top padding

        Object[] selectedItem = new Object[5];
        final String[] selectedId = {""}; // Default to zero

        Integer initialIndex = -1;

        EventHandler<ActionEvent> cbContentNameAction = event -> {
            System.out.println(event);
            ComboBox<CommonDTO> comboBox = (ComboBox<CommonDTO>) event.getSource();
            System.out.println(comboBox.getItems());
            selectedItem[0] = comboBox.getValue();

            System.out.println(selectedItem[0]);


            for (CommonDTO val : comboBox.getItems()) {
                if (val.equals(selectedItem[0])) {
                    System.out.println("selected id" + val.getId());
                    selectedId[0] = val.getId();
                }
            }

        };

        ComboBox<CommonDTO> cbContentName = createComboBox("Select Type", cbContentNameAction);
        if (title == "Zone") {
            cbContentName.setItems(FXCollections.observableArrayList(getIndianStateData()));
        }
        if (title == "Region") {
            cbContentName.setItems(FXCollections.observableArrayList(getAllZonesData()));
        }
        if (title == "District") {
            cbContentName.setItems(FXCollections.observableArrayList(getAllRegions()));
        }
        cbContentName.setPrefWidth(300);
        cbContentName.setMaxWidth(300);
        cbContentName.setMinWidth(300);

        AutoCompleteBox autoCompleteBox1 = new AutoCompleteBox(cbContentName, initialIndex);

        Label lblLabel1 = new Label();
        if (title == "Zone") {
            lblLabel1.setText("State");
        }
        if (title == "Region") {
            lblLabel1.setText("Zone");
        }
        if (title == "District") {
            lblLabel1.setText("Region");
        }

        lblLabel1.setMinWidth(120);
        lblLabel1.setMaxWidth(120);
        lblLabel1.setPrefWidth(120);
        lblLabel1.setPadding(new Insets(0, 0, 0, 30));  // Top, Right, Bottom, Left

        lblLabel1.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Font size 14 and bold


        Label lblLabel2 = new Label();
        lblLabel2.setText(title);
        lblLabel2.setMinWidth(120);
        lblLabel2.setMaxWidth(120);
        lblLabel2.setPrefWidth(120);
        lblLabel2.setPadding(new Insets(0, 0, 0, 30));  // Top, Right, Bottom, Left

        lblLabel2.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-margin-left:5px;"); // Font size 14 and bold


        TextField tfText = new TextField();
        tfText.setPromptText("Enter " + title);
        tfText.setPrefWidth(300);
        tfText.setMaxWidth(300);
        tfText.setMinWidth(300);

        Button buttonConten = createButtonWithImage();

        Insets Margin = new Insets(0, 30, 0, 1);
        HBox.setMargin(buttonConten, Margin);

        HBox.setMargin(tfText, Margin);
        hbox_center.getChildren().addAll(lblLabel1, cbContentName, lblLabel2, tfText);

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        HBox hbox_bottom = new HBox();
        hbox_bottom.setMinWidth(998);
        hbox_bottom.setMaxWidth(998);
        hbox_bottom.setPrefWidth(998);
        hbox_bottom.setMaxHeight(40);
        hbox_bottom.setMinHeight(4);
        hbox_bottom.setPrefHeight(40);
        hbox_bottom.setSpacing(10);
        hbox_bottom.setStyle("-fx-background-radius: 0 0 5 5; -fx-background-color: #FFFFFF;");
        hbox_bottom.setAlignment(Pos.TOP_RIGHT);

        Button addButton = new Button("Submit");
        Button clButton = new Button("Cancel");
        addButton.setId("sub");
        clButton.setId("can");
        hbox_bottom.setMargin(clButton, new Insets(0, 10, 0, 0));

        hbox_bottom.getChildren().addAll(addButton, clButton);
        vBox.getChildren().addAll(hbox_bottom);
        borderPane.setTop(hbox_top);
        borderPane.setCenter(hbox_center);
        borderPane.setBottom(vBox);

        Node[] nodes = new Node[]{cbContentName, buttonConten, addButton};
        CommonValidationsUtils.setupFocusNavigation(nodes);


        Scene scene = new Scene(borderPane, 1000, 230);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);
        primaryStage.centerOnScreen();

        primaryStage.show();


        Platform.runLater(() -> {
            cbContentName.requestFocus();
        });


        cbContentName.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.ENTER || actionEvent.getCode() == KeyCode.TAB && !actionEvent.isShiftDown()) {
                tfText.requestFocus();
            }
        });

        tfText.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.ENTER || actionEvent.getCode() == KeyCode.TAB && !actionEvent.isShiftDown()) {
                addButton.requestFocus();
            }
        });
        addButton.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.LEFT || actionEvent.getCode() == KeyCode.RIGHT) {
                clButton.requestFocus();
            }
            if (actionEvent.getCode() == KeyCode.TAB && actionEvent.isShiftDown()) {
                tfText.requestFocus();
            }
        });

        clButton.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.RIGHT || actionEvent.getCode() == KeyCode.LEFT) {
                addButton.requestFocus();
            }
            if (actionEvent.getCode() == KeyCode.TAB && actionEvent.isShiftDown()) {
                tfText.requestFocus();
            }
        });

//        CommonFunctionalUtils.cursorMomentForComboBox(cbContentName);
//        CommonFunctionalUtils.cursorMomentForTextField(tfText);
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                selectedItem[1] = selectedId[0];
                selectedItem[2] = tfText.getText();
                System.out.println(selectedItem);
                System.out.println(selectedId[0]);


                if (title == "Zone") {
                    insertIntoZoneTable(selectedItem, stage, primaryStage, callback);
                }
                if (title == "Region") {
                    insertIntoRegionTable(selectedItem, stage, primaryStage, callback);
                }
                if (title == "District") {
                    insertIntoDistrictTable(selectedItem, stage, primaryStage, callback);
                }


            }
        });

        clButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });


    }

    private static void insertIntoZoneTable(Object[] selectedItem, Stage stage, Stage
            primaryStage, Consumer<Object[]> callback) {
        Map<String, String> map = new HashMap<>();

        map.put("zoneName", String.valueOf(selectedItem[2]));
        map.put("stateCode", String.valueOf(selectedItem[1]));

        Gson gson = new Gson();

        // Convert HashMap to JSON and print it
        String json = gson.toJson(map);
        System.out.println(json);

        String finalReq = Globals.mapToString(map);
        System.out.println("FinalReq=> i am in" + finalReq);

//        HttpResponse<String> response;
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("formData" + formData);

        AlertUtility.CustomCallback callbackNoti = number -> {
            if (number == 1) {
                HttpResponse<String> response;
                response = APIClient.postFormDataRequest(formData, EndPoints.CREATE_ZONE);


                JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                System.out.println("Response=>" + responseBody);
                message = responseBody.get("message").getAsString();

                if (responseBody.get("responseStatus").getAsInt() == 200) {


                    AlertUtility.CustomCallback callbackNoti1 = number1 -> {
                        if (number1 == 1) {
                            System.out.println("done");
                            primaryStage.close();
                            OverlaysEffect.removeOverlaysEffect(stage);
                            callback.accept(selectedItem);

//                            response = APIClient.postMultipartRequest(map, EndPoints.updateAreaHead, headers);
//                            GlobalController.getInstance().addTabStatic(SALES_CHALLAN_LIST_SLUG, false);
                        } else {
                            System.out.println("working!");
                        }
                    };


                    AlertUtility.AlertSuccess(AlertUtility.alertTypeConfirmation, responseBody.get("message").getAsString(), callbackNoti1);

//                    tfSalesChallanLedgerName.setText("");
//            cmbSalesChallanSupplierGST.getSelectionModel().clearSelection();


                }
            } else {
                System.out.println("working!");
            }
        };

        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Submit", callbackNoti);


    }

    private static void insertIntoRegionTable(Object[] selectedItem, Stage stage, Stage
            primaryStage, Consumer<Object[]> callback) {
        Map<String, String> map = new HashMap<>();

        map.put("regionName", String.valueOf(selectedItem[2]));
        map.put("zoneCode", String.valueOf(selectedItem[1]));

        Gson gson = new Gson();

        // Convert HashMap to JSON and print it
        String json = gson.toJson(map);
        System.out.println(json);

        String finalReq = Globals.mapToString(map);
        System.out.println("FinalReq=> i am in" + finalReq);

//        HttpResponse<String> response;
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("formData" + formData);

        AlertUtility.CustomCallback callbackNoti = number -> {
            if (number == 1) {
                HttpResponse<String> response;
                response = APIClient.postFormDataRequest(formData, EndPoints.CREATE_REGION);


                JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                System.out.println("Response=>" + responseBody);
                message = responseBody.get("message").getAsString();

                if (responseBody.get("responseStatus").getAsInt() == 200) {


                    AlertUtility.CustomCallback callbackNoti1 = number1 -> {
                        if (number1 == 1) {
                            System.out.println("done");
                            primaryStage.close();
                            OverlaysEffect.removeOverlaysEffect(stage);
                            callback.accept(selectedItem);
//                            response = APIClient.postMultipartRequest(map, EndPoints.updateAreaHead, headers);
//                            GlobalController.getInstance().addTabStatic(SALES_CHALLAN_LIST_SLUG, false);
                        } else {
                            System.out.println("working!");
                        }
                    };


                    AlertUtility.AlertSuccess(AlertUtility.alertTypeConfirmation, responseBody.get("message").getAsString(), callbackNoti1);

//                    tfSalesChallanLedgerName.setText("");
//            cmbSalesChallanSupplierGST.getSelectionModel().clearSelection();


                }
            } else {
                System.out.println("working!");
            }
        };

        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Submit", callbackNoti);


    }

    private static void insertIntoDistrictTable(Object[] selectedItem, Stage stage, Stage
            primaryStage, Consumer<Object[]> callback) {
        Map<String, String> map = new HashMap<>();

        map.put("districtName", String.valueOf(selectedItem[2]));
        map.put("regionCode", String.valueOf(selectedItem[1]));

        Gson gson = new Gson();

        // Convert HashMap to JSON and print it
        String json = gson.toJson(map);
        System.out.println(json);

        String finalReq = Globals.mapToString(map);
        System.out.println("FinalReq=> i am in" + finalReq);

//        HttpResponse<String> response;
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("formData" + formData);

        AlertUtility.CustomCallback callbackNoti = number -> {
            if (number == 1) {
                HttpResponse<String> response;
                response = APIClient.postFormDataRequest(formData, EndPoints.CREATE_DISTRICT);


                JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                System.out.println("Response=>" + responseBody);
                message = responseBody.get("message").getAsString();

                if (responseBody.get("responseStatus").getAsInt() == 200) {


                    AlertUtility.CustomCallback callbackNoti1 = number1 -> {
                        if (number1 == 1) {
                            System.out.println("done");
                            primaryStage.close();
                            OverlaysEffect.removeOverlaysEffect(stage);
                            callback.accept(selectedItem);
//                            response = APIClient.postMultipartRequest(map, EndPoints.updateAreaHead, headers);
//                            GlobalController.getInstance().addTabStatic(SALES_CHALLAN_LIST_SLUG, false);
                        } else {
                            System.out.println("working!");
                        }
                    };


                    AlertUtility.AlertSuccess(AlertUtility.alertTypeConfirmation, responseBody.get("message").getAsString(), callbackNoti1);

//                    tfSalesChallanLedgerName.setText("");
//            cmbSalesChallanSupplierGST.getSelectionModel().clearSelection();


                }
            } else {
                System.out.println("working!");
            }
        };

        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Submit", callbackNoti);


    }


    public static <T> void openBatchPopUpForSoToSc(Stage stage, String title, Consumer<Object[]>
            callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<TranxLedgerWindowDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
//        SwitchButton switchButton = new SwitchButton();
//        Label titleLabel = new Label("Patient");
        // Set the font size
//        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
//        hbox_top.getChildren().add(switchButton);
//        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        Button addButton = new Button("+ Add");
        addButton.setMinWidth(120);
        addButton.setMaxWidth(120);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("sub");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
//        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setMinHeight(0);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
//        HBox.setMargin(search, new Insets(0, 0, 0, 10));
//        hbox_top1.getChildren().addAll(search);
//        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedItem = new Object[19];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxBatchWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxBatchWindowDTO, String> b_no = new TableColumn<>("Batch ");
        TableColumn<TranxBatchWindowDTO, String> pur_date = new TableColumn<>("Pur Date");
        TableColumn<TranxBatchWindowDTO, String> manufacturing_date = new TableColumn<>("Mfg.Date");
        TableColumn<TranxBatchWindowDTO, String> b_expiry = new TableColumn<>("Expiry Date");
        TableColumn<TranxBatchWindowDTO, String> mrp = new TableColumn<>("MRP");
        TableColumn<TranxBatchWindowDTO, String> purRate = new TableColumn<>("Purchase Rate");
        TableColumn<TranxBatchWindowDTO, String> b_qty = new TableColumn<>("Quantity");
        TableColumn<TranxBatchWindowDTO, String> b_freeQty = new TableColumn<>("Free Qty");
        TableColumn<TranxBatchWindowDTO, String> b_dis_per = new TableColumn<>("Disc %");
        TableColumn<TranxBatchWindowDTO, String> b_dis_amt = new TableColumn<>("Disc Amt");
        TableColumn<TranxBatchWindowDTO, String> barcode = new TableColumn<>("Barcode");
        TableColumn<TranxBatchWindowDTO, String> margin = new TableColumn<>("Margin %");
        TableColumn<TranxBatchWindowDTO, String> b_rate_a = new TableColumn<>("FSR");
        TableColumn<TranxBatchWindowDTO, String> b_rate_b = new TableColumn<>("CSR");
        TableColumn<TranxBatchWindowDTO, String> b_rate_c = new TableColumn<>("Sale Rate");
        TableColumn<TranxBatchWindowDTO, String> action = new TableColumn<>("Action");


        tableView.getColumns().addAll(b_no, pur_date, manufacturing_date, b_expiry, mrp, purRate, b_qty, b_freeQty, b_dis_per, b_dis_amt, barcode, margin, b_rate_a, b_rate_b, b_rate_c, action);

        b_no.setCellValueFactory(new PropertyValueFactory<>("b_no"));
        pur_date.setCellValueFactory(new PropertyValueFactory<>("pur_date"));
        manufacturing_date.setCellValueFactory(new PropertyValueFactory<>("manufacturing_date"));
        b_expiry.setCellValueFactory(new PropertyValueFactory<>("b_expiry"));
        mrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
        purRate.setCellValueFactory(new PropertyValueFactory<>("PurRate"));
        b_qty.setCellValueFactory(new PropertyValueFactory<>("b_qty"));
        b_freeQty.setCellValueFactory(new PropertyValueFactory<>("b_freeQty"));
        b_dis_per.setCellValueFactory(new PropertyValueFactory<>("b_dis_per"));
        b_dis_amt.setCellValueFactory(new PropertyValueFactory<>("b_dis_amt"));
        barcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        margin.setCellValueFactory(new PropertyValueFactory<>("margin"));
        b_rate_a.setCellValueFactory(new PropertyValueFactory<>("b_rate_a"));
        b_rate_b.setCellValueFactory(new PropertyValueFactory<>("b_rate_b"));
        b_rate_c.setCellValueFactory(new PropertyValueFactory<>("b_rate_c"));


        fetchDataOfProductAllBatches();
        tableView.setItems(observableListBatch);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
//        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                getPatient("", "SD"); // Fetch only SD type ledgers
//            } else {
//                getPatient("", ""); // Fetch all ledgers
//            }
//        });
        tableView.setItems(observableListBatch);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListPatient.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            getPatient(newValue,"");
        });*/
//        search.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent keyEvent) {
//                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
//                if (searchKey.length() >= 3) {
//                    getPatient(searchKey, "");
//                } else if (searchKey.isEmpty()) {
//                    getPatient("", "");
//                }
//                tableView.setItems(observableListPatient);
//            }
//        });

        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxBatchWindowDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxBatchWindowDTO batchWindowDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = batchWindowDTO.getB_no();
                        selectedItem[1] = batchWindowDTO.getPur_date();
                        selectedItem[2] = batchWindowDTO.getManufacturing_date();
                        selectedItem[3] = batchWindowDTO.getB_expiry();
                        selectedItem[4] = batchWindowDTO.getMrp();
                        selectedItem[5] = batchWindowDTO.getPurRate();
                        selectedItem[6] = batchWindowDTO.getB_qty();
                        selectedItem[7] = batchWindowDTO.getB_freeQty();
                        selectedItem[8] = batchWindowDTO.getB_dis_per();
                        selectedItem[9] = batchWindowDTO.getB_dis_amt();
                        selectedItem[10] = batchWindowDTO.getBarcode();
                        selectedItem[11] = batchWindowDTO.getMargin();
                        selectedItem[12] = batchWindowDTO.getB_rate_a();
                        selectedItem[13] = batchWindowDTO.getB_rate_b();
                        selectedItem[14] = batchWindowDTO.getB_rate_c();
                        selectedItem[15] = batchWindowDTO.getCosting();
                        selectedItem[16] = batchWindowDTO.getCostingWithTax();
                        selectedItem[17] = batchWindowDTO.getB_details_id();
                        selectedItem[18] = batchWindowDTO.getProduct_id();


                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
    }

    //Batch List

    private static void fetchDataOfProductAllBatches() {
        System.out.println("productIdSelected in singleInputDialogue " + productIdSelected);
        try {
            observableListBatch.clear();
            Map<String, String> body = new HashMap<>();
            body.put("product_id", productIdSelected); // Assuming search is not dependent on ledger type
            body.put("level_a_id", ""); // Assuming search is not dependent on ledger type
            body.put("unit_id", "1"); // Assuming search is not dependent on ledger type
            body.put("invoice_date", "2024-03-23"); // Assuming search is not dependent on ledger type
            String requestBody = Globals.mapToStringforFormData(body);
            System.out.println("requestBody" + requestBody);
            HttpResponse<String> response = APIClient.postFormDataRequest(requestBody, "get_Product_batch");
            System.out.println("response" + response);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
//            ObservableList<TranxBatchWindowDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("data").getAsJsonArray();
//                System.out.println("responseArray"+responseArray);

                if (responseArray.size() > 0) {
//                    tblvSalesChallanBatWinCreTableview.setVisible(true);
//                    tblvSalesChallanAdditionalChargesLedWindtableview.setVisible(true);
                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
//
//                        String b_no = item.get("id").getAsString();
//                        String product_name = item.get("product_name").getAsString();
//                        String b_expiry = item.get("unique_code").getAsString();
//                        String expiry_date = item.get("expiry_date").getAsString();


                        String id = item.get("id").getAsString();
                        String b_details_id = item.get("b_details_id").getAsString();
                        String org_b_details_id = item.get("org_b_details_id").getAsString();
                        String product_name = item.get("product_name").getAsString();
                        String batch_no = !item.get("batch_no").isJsonNull() ? item.get("batch_no").getAsString() : "";
//                        if(item.get("qty").getAsString()!=null){
//                            String qty = item.get("qty").getAsString();
//                        }
//
//                        String qty = item.get("qty").getAsString()!=null?item.get("qty").getAsString():"";
                        String qty = "0";
                        String free_qty = item.get("free_qty").getAsString();

                        String expiry_date = item.get("expiry_date").getAsString();
//                        String expiry_date = "2025-02-25";
                        LocalDate expiryDate = LocalDate.parse("2025-02-25");

                        String purchase_rate = item.get("purchase_rate").getAsString();
                        String sales_rate = item.get("sales_rate").getAsString();
                        String mrp = item.get("mrp").getAsString();
                        String min_rate_a = item.get("min_rate_a").getAsString();
                        String min_rate_b = item.get("min_rate_b").getAsString();
                        String min_rate_c = item.get("min_rate_c").getAsString();
                        String net_rate = item.get("net_rate").getAsString();
                        String sales_rate_with_tax = item.get("sales_rate_with_tax").getAsString();


                        String manufacturing_date = item.get("manufacturing_date").getAsString();
                        LocalDate manufacturingDate = LocalDate.parse("2023-12-12");

                        String min_margin = item.get("min_margin").getAsString();
                        String b_rate = item.get("b_rate").getAsString();
//                        String expiry_date = item.get("expiry_date").getAsString();
                        String costing = item.get("costing").getAsString();
                        String costingWithTax = item.get("costingWithTax").getAsString();
                        String closing_stock = item.get("closing_stock").getAsString();
                        String opening_stock = item.get("opening_stock").getAsString();
//                        String min_margin = item.get("min_margin").getAsString();
                        String max_discount = item.get("max_discount").getAsString();
                        String min_discount = item.get("min_discount").getAsString();
                        String dis_per = item.get("dis_per").getAsString();
                        String dis_amt = item.get("dis_amt").getAsString();
                        String cess_per = item.get("cess_per").getAsString();
                        String cess_amt = item.get("cess_amt").getAsString();
                        String barcode = item.get("barcode").getAsString();


                        String purchase_date = item.get("pur_date").getAsString();
                        LocalDate purDate = LocalDate.now();

                        String tax_per = "";
                        //String tax_per = item.get("tax_per").getAsString();


                        observableListBatch.add(new TranxBatchWindowDTO(batch_no, purDate, manufacturingDate, expiryDate, mrp, purchase_rate, qty, free_qty, dis_per,
                                dis_amt, barcode, min_margin, min_rate_a, min_rate_b, min_rate_c, costing, costingWithTax, b_details_id, "", "", "", "", "1"));

                    }


//                    tblcCounterSaleBatWinBatchNo.setCellValueFactory(new PropertyValueFactory<>("b_no"));
//                    tblcCounterSaleBatWinPurDate.setCellValueFactory(new PropertyValueFactory<>("pur_date"));
//                    tblcCounterSaleBatWinMFGDate.setCellValueFactory(new PropertyValueFactory<>("manufacturing_date"));
//                    tblcCounterSaleBatWinEXPDate.setCellValueFactory(new PropertyValueFactory<>("b_expiry"));
//                    tblcCounterSaleBatWinMRP.setCellValueFactory(new PropertyValueFactory<>("mrp"));
//                    tblcCounterSaleBatWinPurRate.setCellValueFactory(new PropertyValueFactory<>("purRate"));
//                    tblcCounterSaleBatWinQty.setCellValueFactory(new PropertyValueFactory<>("b_qty"));
//                    tblcCounterSaleBatWinFreeQty.setCellValueFactory(new PropertyValueFactory<>("b_freeQty"));
//                    tblcCounterSaleBatWinDisPer.setCellValueFactory(new PropertyValueFactory<>("b_dis_per"));
//                    tblcCounterSaleBatWinDisAmt.setCellValueFactory(new PropertyValueFactory<>("b_dis_amt"));
//                    tblcCounterSaleBatWinBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
//                    tblcCounterSaleBatWinMarginPer.setCellValueFactory(new PropertyValueFactory<>("margin"));
//                    tblcCounterSaleBatWinFSR.setCellValueFactory(new PropertyValueFactory<>("b_rate_a"));
//                    tblcCounterSaleBatWinCSR.setCellValueFactory(new PropertyValueFactory<>("b_rate_b"));
//                    tblcCounterSaleBatWinSaleRate.setCellValueFactory(new PropertyValueFactory<>("b_rate_c"));
//
//
//                    tblvCounterSaleBatWinCreTableview.setItems(observableList);
//                    tblvSalesChallanAdditionalChargesLedWindtableview.setItems(observableList);

                } else {
                    System.out.println("responseObject is null");
                }
            } else {
                System.out.println("Error in response");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static <T> void billByBillReceipt(Stage stage, String title, String
            ledgerId, Consumer<Object[]> callback) {
        BillByBillPerticulars particular = new BillByBillPerticulars();
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        HBox hBox_center = new HBox();
        HBox hBox_bottom = new HBox();
        HBox hBox_center1 = new HBox();


        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(30);
        hbox_top.setMinHeight(30);
        hbox_top.setPrefHeight(30);

        hBox_center.setMinWidth(978);
        hBox_center.setMaxWidth(978);
        hBox_center.setPrefWidth(978);
        hBox_center.setMaxHeight(0);
        hBox_center.setMinHeight(0);
        hBox_center.setPrefHeight(0);


        hBox_center1.setMinWidth(978);
        hBox_center1.setMaxWidth(978);
        hBox_center1.setPrefWidth(978);
        hBox_center1.setMaxHeight(30);
        hBox_center1.setMinHeight(30);
        hBox_center1.setPrefHeight(30);


        hBox_bottom.setMinWidth(978);
        hBox_bottom.setMaxWidth(978);
        hBox_bottom.setPrefWidth(978);
        hBox_bottom.setMaxHeight(20);
        hBox_bottom.setMinHeight(20);
        hBox_bottom.setPrefHeight(20);


        Label opening_balance = new Label("Opening Balance");
        opening_balance.setPadding(new Insets(38, 40, 40, 10));
        opening_balance.setStyle("-fx-font-size: 14; -fx-text-fill: #404040; -fx-font-weight: bold;");


        Label AdvancedAmt = new Label("Advanced Amt");
        AdvancedAmt.setStyle("-fx-font-size: 14;");
        AdvancedAmt.setPadding(new Insets(10, 10, 0, 0));


        Label RemainingAmt = new Label("Remaining Amt");
        RemainingAmt.setStyle("-fx-font-size: 14;");
        RemainingAmt.setPadding(new Insets(10, 10, 0, 10));

        Label payableAmt = new Label("payable Amt");
        payableAmt.setStyle("-fx-font-size: 14;");
        payableAmt.setPadding(new Insets(10, 10, 0, 10));


        Label SelectedAmt = new Label("Selected Amt");
        SelectedAmt.setStyle("-fx-font-size: 14;");
        SelectedAmt.setPadding(new Insets(10, 10, 0, 10));

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(10, 10, 8, 10));

        TextField textField = new TextField();
        textField.setPadding(new Insets(10, 10, 0, 10));

        TextField textField1 = new TextField();
        textField1.setPadding(new Insets(10, 10, 0, 10));


        CheckBox checkBox = new CheckBox();
        checkBox.setPadding(new Insets(10, 10, 0, 10));


        TextField textField2 = new TextField();
        textField2.setPadding(new Insets(10, 10, 0, 10));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 8, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        //SwitchButton switchButton = new SwitchButton();
        //Label titleLabel = new Label("Debitor");
        // Set the font size
        // titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        //vbox_top.setAlignment(Pos.TOP_LEFT);
        hBox_center.getChildren().add(payableAmt);
        hBox_center.getChildren().add(textField);
        hBox_center.getChildren().add(SelectedAmt);
        hBox_center.getChildren().add(textField1);
        hBox_center.getChildren().add(RemainingAmt);
        hBox_center.getChildren().add(textField2);
        hBox_center.getChildren().add(checkBox);
        hBox_center.getChildren().add(AdvancedAmt);

        hBox_center1.getChildren().add(opening_balance);


        //hbox_top.getChildren().add(switchButton);
        // hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();
        //Button addButton = new Button("+ Add Ledger");
//        addButton.setMinWidth(120);
//        addButton.setMaxWidth(120);
//        addButton.setMinHeight(30);
//        addButton.setMaxHeight(30);
//        addButton.setId("submit-btn");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        VBox.setMargin(payableAmt, new Insets(0, 0, 0, 10));
        // HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        //hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setMinHeight(0);
        hbox_top1.setMaxHeight(0);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        // TextField search = new TextField("");
        // search.setPromptText("Search");
        //search.setPadding(new Insets(10, 0, 0, 10));
        //  search.setPrefWidth(350);
//HBox.setMargin(search, new Insets(0, 0, 0, 10));
        //hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1, hBox_center, hBox_center1, hBox_bottom);
        Object[] selectedItem = new Object[5];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout...............................................................................................
        ScrollPane scrollPane = new ScrollPane();
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        HBox hBox1 = new HBox();
        HBox hBox2 = new HBox();
        HBox hBox3 = new HBox();
        HBox hBox4 = new HBox();
        HBox hBox5 = new HBox();
        HBox bottom = new HBox();


        hBox.setMinWidth(978);
        hBox.setMaxWidth(978);
        hBox.setPrefWidth(978);
        hBox.setMaxHeight(40);
        hBox.setMinHeight(40);
        hBox.setPrefHeight(40);
        hBox.setPadding(new Insets(10, 100, 10, 10));


        bottom.setMinWidth(978);
        bottom.setMaxWidth(978);
        bottom.setPrefWidth(978);
        bottom.setMaxHeight(30);
        bottom.setMinHeight(30);
        bottom.setPrefHeight(30);
        bottom.setAlignment(Pos.CENTER_RIGHT);


        Button button = new Button("sumbit");
        Button button1 = new Button("Cancel");
        button.setStyle("-fx-background-color: #21c78a;");

        Label Total = new Label("Total");
        Total.setStyle("-fx-font-size: 14;");

        Label sales_invoice = new Label("Sales Invoice");
        sales_invoice.setStyle("-fx-font-size: 14;-fx-text-fill: #404040; -fx-font-weight: bold;");
        sales_invoice.setPadding(new Insets(25, 10, 0, 10));

        Label Total1 = new Label("Total");
        Total1.setStyle("-fx-font-size: 14;");

        Label credit_note = new Label("Credit Note");
        credit_note.setStyle("-fx-font-size: 14; -fx-text-fill: #404040; -fx-font-weight: bold;");
        credit_note.setPadding(new Insets(25, 10, 0, 10));

        Label Total2 = new Label("Total");
        Total2.setStyle("-fx-font-size: 14;");


        Label GrandTotal = new Label("Grand Total");
        GrandTotal.setStyle("-fx-font-size: 14;-fx-text-fill: #404040; -fx-font-weight: bold;");
        GrandTotal.setPadding(new Insets(25, 10, 0, 10));

        Label GoToReceipt = new Label("Go To Receipt ");
        GoToReceipt.setStyle("-fx-font-size: 14;-fx-text-fill: #404040; -fx-font-weight: bold;");
        GoToReceipt.setPadding(new Insets(25, 10, 0, 10));


        hBox.getChildren().addAll(Total);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setStyle("-fx-background-color:  #D2F6E9;");
        hBox2.setPrefHeight(30);
        hBox1.getChildren().addAll(sales_invoice);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox2.getChildren().addAll(Total1);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        hBox2.setStyle("-fx-background-color:  #D2F6E9;");
        hBox2.setPrefHeight(30);
        hBox3.getChildren().addAll(credit_note);
        hBox3.setAlignment(Pos.CENTER_LEFT);
        hBox4.getChildren().addAll(Total2);
        hBox4.setAlignment(Pos.CENTER_LEFT);
        hBox4.setStyle("-fx-background-color:  #D2F6E9;");
        hBox2.setPrefHeight(30);
        hBox5.getChildren().addAll(GrandTotal, GoToReceipt);
        hBox5.setStyle("-fx-background-color: #A0EFD2;");
        hBox5.setAlignment(Pos.CENTER_LEFT);
        bottom.getChildren().addAll(button, button1);

        TableView<TranxReceiptWindowDTO> tblSalesInvoice = getTableView(ledgerId, "sales_invoice");
        TableView<TranxReceiptWindowDTO> tblOpeningInvoice = getTableView(ledgerId, "opening_balance");
        TableView<TranxReceiptWindowDTO> tblCreditNote = getTableView(ledgerId, "credit_note");
        vBox.getChildren().addAll(tblOpeningInvoice, hBox, hBox1, tblSalesInvoice, hBox2, hBox3, tblCreditNote, hBox4, hBox5, bottom);
        //Filter Ledger by type
//        //switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                fetchDataOfAllTransactionLedgers("", "SD"); // Fetch only SD type ledgers
//            } else {
//                fetchDataOfAllTransactionLedgers("", ""); // Fetch all ledgers
//            }
//        });
//        tableView.setItems(observableBillByBill);
        // Add a listener to the text property of the search TextField
//        search.textProperty().addListener((observable, oldValue, newValue) -> {
//            // Filter the items based on the newValue
//            tableView.setItems(observableListLedger.filtered(item ->
//                    item.getLedger_name().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getCode().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getLedger_group().toLowerCase().contains(newValue.toLowerCase()) ||
//                    item.getContact_number().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getCurrent_balance().toLowerCase().contains(newValue.toLowerCase())||
//                    item.getType().toLowerCase().contains(newValue.toLowerCase())
//                    ));
//        });

       /* search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue for ledger name and contact number
            fetchDataOfAllTransactionLedgers(newValue,"");
        });*/
//       // search.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent keyEvent) {
//                String searchKey = search.getText().trim();
//                System.out.println("i am " + searchKey);
//                if (searchKey.length() >= 3) {
//                    fetchDataOfAllTransactionLedgers(searchKey, "");
//                } else if (searchKey.isEmpty()) {
//                    fetchDataOfAllTransactionLedgers("", "");
//                }
//            }
//        });
        scrollPane.setContent(vBox);
        borderPane.setTop(vbox_top);
        borderPane.setCenter(scrollPane);
        //Double click on ledger list
//        tableView.setRowFactory(tv -> {
//            TableRow<TranxReceiptWindowDTO> row = new TableRow<>();
//            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent mouseEvent) {
//                    if (mouseEvent.getClickCount() == 2) {
////                        TranxLedgerWindowDTO ledgerWindowDTO = tableView.getSelectionModel().getSelectedItem();
////                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
////                        selectedItem[0] = ledgerWindowDTO.getLedger_name();
////                        selectedItem[1] = ledgerWindowDTO.getId();
////                        selectedItem[2] = gstDetails;
////                        selectedItem[3] = ledgerWindowDTO.getStateCode();
////                        selectedItem[4] = ledgerWindowDTO.getBalancingMethod();
//                        callback.accept(selectedItem);
//                        primaryStage.close();
//                        OverlaysEffect.removeOverlaysEffect(stage);
//                    }
//                }
//            });
//            return row;
//        });
        Scene scene = new Scene(borderPane, 980, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();

    }

    private static <
            BooleanProperty> TableView<TranxReceiptWindowDTO> getTableView(String ledgerId, String cndSlug) {
        ObservableList<TranxReceiptWindowDTO> tblLst = fetchDataOfbillByBill(ledgerId, cndSlug);
        System.out.println("observableBillByBill " + observableBillByBill);
        System.out.println("cndSlug " + cndSlug);
        TableView<TranxReceiptWindowDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(200);
//        tableView.setMaxHeight(200);
        tableView.setMinHeight(200);
        tableView.setPrefWidth(958);
        TableColumn<TranxReceiptWindowDTO, String> isSelect = new TableColumn<>("isSelect");
        TableColumn<TranxReceiptWindowDTO, String> InvoiceNo = new TableColumn<>("Invoice No");
        TableColumn<TranxReceiptWindowDTO, String> InvoiceAmt = new TableColumn<>("Invoice Amt");
        TableColumn<TranxReceiptWindowDTO, String> BillDate = new TableColumn<>("Bill Date");
        TableColumn<TranxReceiptWindowDTO, String> Balance = new TableColumn<>("Balance");
        TableColumn<TranxReceiptWindowDTO, String> Type = new TableColumn<>("Type");
//        TableColumn<TranxReceiptWindowDTO, String> DueDate = new TableColumn<>("Due Date");
        TableColumn<TranxReceiptWindowDTO, String> Day = new TableColumn<>("Day");
        TableColumn<TranxReceiptWindowDTO, String> PaidAmt = new TableColumn<>("Paid Amt");
        TableColumn<TranxReceiptWindowDTO, String> Balance1 = new TableColumn<>("Balance");


        tableView.getColumns().addAll(isSelect, InvoiceNo, InvoiceAmt, BillDate, Balance, Type, Day, PaidAmt, Balance1);


        isSelect.setCellFactory(CheckBoxTableCell.forTableColumn(input -> {
            System.out.println("input =>" + input);
            return null;
        }));


        isSelect.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getIsSelect()));
        InvoiceNo.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getInvoiceNo()));
        InvoiceAmt.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getTotalAmt()));
        BillDate.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getInvoiceDate()));
        Balance.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getAmount()));
        Type.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getBalancingType()));
        Day.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getDueDays()));
        PaidAmt.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getPaidAmt()));
        Balance1.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getRemainingAmt()));
        tableView.setItems(tblLst);

        return tableView;
    }


    public static <T> void openBankPopUp(Stage stage, String title, ReceiptPaymentModeDTO
            inPaymentMode, Consumer<ReceiptPaymentModeDTO> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();

        primaryStage.initOwner(stage);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");

        Platform.runLater(() -> {
            borderPane.requestFocus();
        });


        //BorderPan under Top Layout....................................................................................................................
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(998);
        hbox_top.setMaxWidth(998);
        hbox_top.setPrefWidth(998);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title = LedgerCommonController.createLabel("Bank Account");
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #d9f0fb; -fx-border-color: #ffffff; -fx-border-width: 0 0 3 0;");

        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_center = new HBox();
        hbox_center.setMinWidth(998);
        hbox_center.setMaxWidth(998);
        hbox_center.setPrefWidth(998);
        hbox_center.setAlignment(Pos.CENTER);
        hbox_center.setStyle("-fx-background-color: #e6f2f8;");

        Integer initialIndex = -1;
        ObservableList<CommonOption> paymentMode = FXCollections.observableArrayList(Globals.getPaymentMode());
        Label lbPaymentMode = LedgerCommonController.createLabel("Payment Mode");

        Label lbPaymentModeReq = LedgerCommonController.createLabelWithRequired();


        EventHandler<ActionEvent> handleGstType = event -> {
            ComboBox<T> comboBox = (ComboBox<T>) event.getSource();
            paymentMode.stream().filter(s -> s.getName().equalsIgnoreCase(String.valueOf(comboBox.getValue()))).findAny().ifPresent(ob -> {
                inPaymentMode.setPaymentId(ob.getValue());
                inPaymentMode.setPaymentMode(ob.getName());
            });
        };


        ComboBox<CommonOption> paymentModeComboBox = LedgerCommonController.createComboBox("Payment Mode", handleGstType);
        paymentModeComboBox.setPromptText("Select Payment Mode");
        paymentModeComboBox.setItems(paymentMode);
        AutoCompleteBox autoCompleteBox = new AutoCompleteBox(paymentModeComboBox, initialIndex);
        if (inPaymentMode.getPaymentId() > 0) {
            paymentMode.stream().filter(s -> s.getName().equalsIgnoreCase(String.valueOf(inPaymentMode.getPaymentId()))).findAny().ifPresent(ob -> {
                paymentModeComboBox.setValue(ob);
            });


        }


        paymentModeComboBox.setConverter(new StringConverter<CommonOption>() {
            @Override
            public String toString(CommonOption commonOption) {
                return commonOption != null ? commonOption.getName() : "";
            }

            @Override
            public CommonOption fromString(String s) {
                return null;
            }
        });


        Label lbAccNo = LedgerCommonController.createLabel("RefNumber");
        Label lbAccReq = LedgerCommonController.createLabelWithRequired();
        TextField tfAccNo = LedgerCommonController.createTextField("Reference Number");
        lbAccNo.setPadding(new Insets(10, 10, 10, 10));


        // if (!inBankDetails.getBankAccNo().isEmpty()) tfAccNo.setText(inBankDetails.getBankAccNo());

        Label lbifsc = LedgerCommonController.createLabel("Payment Date");
        Label lbifscReq = LedgerCommonController.createLabelWithRequired();
        TextField tfIfsc = LedgerCommonController.createTextField("DD/MM/YYYY");
        // if (!inBankDetails.getBankIFSCCode().isEmpty()) tfIfsc.setText(inBankDetails.getBankIFSCCode());
        // Adjust the left padding value as needed


        Insets Margin = new Insets(0, 10, 0, 1);

        HBox.setMargin(tfAccNo, Margin);
        HBox.setMargin(tfIfsc, Margin);


        hbox_center.getChildren().addAll(lbPaymentMode, lbPaymentModeReq, paymentModeComboBox, lbAccNo, lbAccReq, tfAccNo, lbifsc, lbifscReq, tfIfsc);

        HBox hbox_bottom = new HBox();
        hbox_bottom.setMinWidth(998);
        hbox_bottom.setMaxWidth(998);
        hbox_bottom.setPrefWidth(998);
        hbox_bottom.setMaxHeight(55);
        hbox_bottom.setMinHeight(55);
        hbox_bottom.setPrefHeight(55);
        hbox_bottom.setSpacing(10);
        hbox_bottom.setStyle("-fx-background-radius: 0 0 5 5; -fx-background-color: white;");
        hbox_bottom.setAlignment(Pos.CENTER_RIGHT);

        Button suButton = new Button("Submit");
        HBox.setMargin(suButton, new Insets(0, 10, 0, 0));
        Button clButton = new Button("Clear");
        suButton.setId("submit-btn");
        clButton.setId("cancel-btn");


        hbox_bottom.getChildren().addAll(clButton, suButton);

        borderPane.setTop(hbox_top);
        borderPane.setCenter(hbox_center);
        borderPane.setBottom(hbox_bottom);

        Node[] nodes = new Node[]{tfAccNo, tfIfsc, suButton};
        CommonValidationsUtils.setupFocusNavigation(nodes);

        Scene scene = new Scene(borderPane, 1000, 170);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();
        primaryStage.setOnShown((e) -> {
            // Platform.runLater(::requestFocus);
        });
        primaryStage.show();


    }

    //Add prescription  pop up window for counter sale

    public static <T> void openAddPrescriptionPopUp(Stage stage, String title, Consumer<Object[]> callback) {
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        ObservableList<CounterSaleAddPrescritDTO> observableLedgerList = FXCollections.observableArrayList();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        Platform.runLater(() -> borderPane.requestFocus());
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(1150
        );
        hbox_top.setMaxWidth(1150);
        hbox_top.setPrefWidth(1150);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(0, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
//        SwitchButton switchButton = new SwitchButton();
//        Label titleLabel = new Label("Patient");
        // Set the font size
//        titleLabel.setFont(new Font("Arial", 16)); // Set the font size to 16
        hbox_top.setAlignment(Pos.CENTER_LEFT);
        hbox_top.getChildren().add(popup_title);
//        hbox_top.getChildren().add(switchButton);
//        hbox_top.getChildren().add(titleLabel);
        Region spacer = new Region();

//        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        //hbox_top.getChildren().add(addButton);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");

        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(1150);
        hbox_top1.setMaxWidth(1150);
        hbox_top1.setPrefWidth(1150);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setPrefHeight(50);
        hbox_top1.setAlignment(Pos.CENTER_LEFT);
        hbox_top1.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #E6F2F8; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 0.5 0;");
        Label lblPrescription = new Label("Prescription");
        lblPrescription.setStyle("-fx-font-size: 15; -fx-text-fill: #404040;-fx-font-weight: bold;");
        HBox.setMargin(lblPrescription, new Insets(0, 0, 0, 8));
        Button btnPrescrit = new Button();
        btnPrescrit.setText("Upload");
        HBox.setMargin(btnPrescrit, new Insets(0, 0, 0, 30));


        HBox hbox_top2 = new HBox();
        hbox_top2.setMinWidth(1150);
        hbox_top2.setMaxWidth(1150);
        hbox_top2.setPrefWidth(1150);
        hbox_top2.setMaxHeight(50);
        hbox_top2.setMinHeight(50);
        hbox_top2.setPrefHeight(50);
        hbox_top2.setAlignment(Pos.CENTER_LEFT);
        hbox_top2.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #E6F2F8; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 0.5 0;");

        Label lblProduct = new Label("Product");
        lblProduct.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblProduct.setPrefWidth(60);
        HBox.setMargin(lblProduct, new Insets(0, 0, 0, 5));

        TextField tfProduct = new TextField();
        System.out.println("Paybale Amt-->" + tfProduct.getText());
        tfProduct.setPromptText("Product Name");
        HBox.setMargin(tfProduct, new Insets(0, 0, 0, 5));
        tfProduct.setPrefWidth(180);

        Label lblPackage = new Label("Pkg");
        lblPackage.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblPackage.setPrefWidth(30);
        HBox.setMargin(lblPackage, new Insets(0, 0, 0, 10));

        TextField tfPackage = new TextField();
        tfPackage.setPromptText("Package");
        HBox.setMargin(tfPackage, new Insets(0, 0, 0, 5));
        tfPackage.setPrefWidth(80);

        Label lblUnit = new Label("Unit");
        lblUnit.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblUnit.setPrefWidth(40);
        HBox.setMargin(lblUnit, new Insets(0, 0, 0, 10));

        TextField tfUnit = new TextField();
        tfUnit.setPromptText("Unit");
        HBox.setMargin(tfUnit, new Insets(0, 0, 0, 5));
        tfUnit.setPrefWidth(80);

        Label lblQty = new Label("Qty");
        lblQty.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblQty.setPrefWidth(40);
        HBox.setMargin(lblQty, new Insets(0, 0, 0, 10));

        TextField tfQty = new TextField();
        tfQty.setPromptText("Qty");
        tfQty.setPrefWidth(80);
        HBox.setMargin(tfQty, new Insets(0, 0, 0, 5));

        Label lblNooftime = new Label("No.of Time");
        lblNooftime.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblNooftime.setPrefWidth(80);
        HBox.setMargin(lblNooftime, new Insets(0, 0, 0, 10));

        TextField tfNooftime = new TextField();
        tfNooftime.setPromptText("No.of time");
        HBox.setMargin(tfNooftime, new Insets(0, 0, 0, 5));
        tfNooftime.setPrefWidth(80);

        Label lblDays = new Label("Days");
        lblDays.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblDays.setPrefWidth(40);
        HBox.setMargin(lblDays, new Insets(0, 0, 0, 10));

        TextField tfDays = new TextField();
        tfDays.setPromptText("Days");
        HBox.setMargin(tfDays, new Insets(0, 0, 0, 5));
        tfDays.setPrefWidth(80);

        Label lblTotal = new Label("Total");
        lblTotal.setStyle("-fx-font-size: 13; -fx-text-fill: #404040;-fx-font-weight: bold;");
        lblTotal.setPrefWidth(50);
        HBox.setMargin(lblTotal, new Insets(0, 0, 0, 10));

        TextField tfTotal = new TextField();
        tfTotal.setPromptText("Total");
        tfTotal.setPrefWidth(80);
        HBox.setMargin(tfTotal, new Insets(0, 0, 0, 5));

        Button addButton = new Button("Add");
        addButton.setMinWidth(60);
        addButton.setMaxWidth(60);
        addButton.setMinHeight(30);
        addButton.setMaxHeight(30);
        addButton.setId("submit-btn");
        HBox.setMargin(addButton, new Insets(0, 10, 0, 10));
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));
//        CheckBox cbAdvanceAmt = new CheckBox("Advance Amt.");
//        cbAdvanceAmt.setStyle("-fx-font-size: 15; -fx-text-fill: #404040;-fx-font-weight: bold;");
//        HBox.setMargin(cbAdvanceAmt, new Insets(0, 0, 0, 30));
        hbox_top1.getChildren().addAll(lblPrescription, btnPrescrit);
        hbox_top2.getChildren().addAll(lblProduct, tfProduct, lblPackage, tfPackage, lblUnit, tfUnit, lblQty, tfQty, lblNooftime, tfNooftime, lblDays, tfDays, lblTotal, tfTotal, addButton);
        /**** END hox_top2 ****/

        //BorderPane Under Center Layout.....................................................................................................
//        HBox hbox_top1 = new HBox();
//        hbox_top1.setMinWidth(1150);
//        hbox_top1.setMaxWidth(978);
//        hbox_top1.setPrefWidth(978);
//        hbox_top1.setMaxHeight(0);
//        hbox_top1.setMinHeight(0);
//        hbox_top1.setMaxHeight(0);
//        hbox_top1.setAlignment(Pos.TOP_LEFT);
//        hbox_top1.setStyle("-fx-background-color: white;");
//        TextField search = new TextField("");
//        search.setPromptText("Search");
//        search.setPadding(new Insets(10, 0, 0, 10));
//        search.setPrefWidth(350);
//        HBox.setMargin(search, new Insets(0, 0, 0, 10));
//        hbox_top1.getChildren().addAll(search);
//        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1, hbox_top2);
        Object[] selectedItem = new Object[19];
        Integer initialIndex = -1;

        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<CounterSaleAddPrescritDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<CounterSaleAddPrescritDTO, String> product = new TableColumn<>("Product ");
        TableColumn<CounterSaleAddPrescritDTO, String> package1 = new TableColumn<>("Package");
        TableColumn<CounterSaleAddPrescritDTO, String> unit = new TableColumn<>("Unit");
        TableColumn<CounterSaleAddPrescritDTO, String> qty = new TableColumn<>("Qty");
        TableColumn<CounterSaleAddPrescritDTO, String> noOfTimes = new TableColumn<>("No.of Times");
        TableColumn<CounterSaleAddPrescritDTO, String> days = new TableColumn<>("Days");
        TableColumn<CounterSaleAddPrescritDTO, String> total = new TableColumn<>("Total");
        TableColumn<CounterSaleAddPrescritDTO, String> action = new TableColumn<>("Action");


        tableView.getColumns().addAll(product, package1, unit, qty, noOfTimes, days, total, action);

        product.setCellValueFactory(new PropertyValueFactory<>("product"));
        package1.setCellValueFactory(new PropertyValueFactory<>("package1"));
        unit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        noOfTimes.setCellValueFactory(new PropertyValueFactory<>("noOfTimes"));
        days.setCellValueFactory(new PropertyValueFactory<>("days"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));
        action.setCellValueFactory(new PropertyValueFactory<>("action"));


        // fetchDataOfProductAllBatches();
        tableView.setItems(observableListCounterSPriscri);
        vBox.getChildren().addAll(tableView);
        //Filter Ledger by type
//        switchButton.switchOnProperty().addListener((observable, oldValue, newValue) -> {
//            // Check the state of the SwitchButton and apply filter accordingly
//            if (newValue) {
//                getPatient("", "SD"); // Fetch only SD type ledgers
//            } else {
//                getPatient("", ""); // Fetch all ledgers
//            }
//        });
        tableView.setItems(observableListCounterSPriscri);


        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<CounterSaleAddPrescritDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        CounterSaleAddPrescritDTO counterSPrescriptionDTO = tableView.getSelectionModel().getSelectedItem();
//                        ObservableList<GstDetailsDTO> gstDetails = ledgerWindowDTO.getGstDetails();
                        selectedItem[0] = counterSPrescriptionDTO.getProduct();
                        selectedItem[1] = counterSPrescriptionDTO.getPackage1();
                        selectedItem[2] = counterSPrescriptionDTO.getUnit();
                        selectedItem[3] = counterSPrescriptionDTO.getQty();
                        selectedItem[4] = counterSPrescriptionDTO.getNoOfTimes();
                        selectedItem[5] = counterSPrescriptionDTO.getDays();
                        selectedItem[6] = counterSPrescriptionDTO.getTotal();


                        callback.accept(selectedItem);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });
            return row;
        });
        Scene scene = new Scene(borderPane, 1150, 540);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

        Platform.runLater(() -> tfProduct.setOnMouseClicked(actionEvent -> {
            SingleInputDialogs.openProductPopUp(stage, "Prescription", input -> {
                System.out.println("i am madam ");
                System.out.println("i am madam " + input[0]);
                System.out.println("i am madam " + input[1]);
                System.out.println("i am madam " + input[2]);
                System.out.println("i am madam " + input[3]);
                tfProduct.setText(input[0].toString());
                tfPackage.setText(input[2].toString());
                tfUnit.setText(input[4].toString());
                tfQty.setText("1");

                //   patientId=String.valueOf(input[0]);

            });

        }));


        btnPrescrit.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();

            // Set extension filters
            FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
            FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx, *.xls)", "*.xlsx", "*.xls");
            FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json");
            FileChooser.ExtensionFilter imgFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png");

            // Add filters to fileChooser
            fileChooser.getExtensionFilters().addAll(textFilter, pdfFilter, excelFilter, jsonFilter, imgFilter);
            // Show open file dialog
            Stage stage1 = (Stage) btnPrescrit.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage1);

            // Handle selected file
            if (selectedFile != null) {
//                tfPurChallChooseFileText.setText(selectedFile.getName());
            }
        });

        Double qty1 = tfQty.getText() != "" ? Double.valueOf(tfQty.getText()) : 0;
        Double noofTime1 = tfNooftime.getText() != "" ? Double.valueOf(tfNooftime.getText()) : 0;
        Double days1 = tfDays.getText() != "" ? Double.valueOf(tfDays.getText()) : 0;

        tfQty.setOnKeyTyped(e -> {
            System.out.println("tfqty");
            Double total1 = handleCalculation(qty1, noofTime1, days1);
            tfTotal.setText(total1.toString());
        });
        tfNooftime.setOnKeyTyped(e -> {

            Double total1 = handleCalculation(qty1, noofTime1, days1);
            tfTotal.setText(total1.toString());
        });
        tfDays.setOnKeyTyped(e -> {
            System.out.println("tfqty2345");
            Double total1 = handleCalculation(qty1, noofTime1, days1);
            tfTotal.setText(total1.toString());
        });


    }

    public static Double handleCalculation(Double qty, Double noofTime, Double days) {

        Double total1 = (qty * noofTime * days);
        return total1;
    }

    public static List<UnitRateList> getUnitRateListFromProduct(String productName) {
        fetchDataOfAllTransactionProducts(productName);
        if (observableListProduct.size() > 0) {
            TranxProductWindowDTO productWindowDTO = observableListProduct.get(0);
            return productWindowDTO.getUnitRateList();
        }
        return new ArrayList<>();
    }

    public static <T> void openInvoiceListSalesRtn(Long ledgerId, Stage stage, String title, Consumer<Object[]>
            callback) {
        System.out.println("Title in Ledger List:" + title);
        OverlaysEffect.setOverlaysEffect(stage);
        Stage primaryStage = new Stage();
        primaryStage.initOwner(stage); // Set the owner stage
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);

        //Main Layout................................................................................................................................
        BorderPane borderPane = new BorderPane();
        borderPane.getStylesheets().add(GenivisApplication.class.getResource("/com/opethic/genivis/ui/css/popup_for_catalog.css").toExternalForm());
        borderPane.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #bfbfc0; -fx-border-radius: 5; -fx-border-width: 0.8;");
        //BorderPan under Top Layout....................................................................................................................
        VBox vbox_top = new VBox();
        HBox hbox_top = new HBox();
        hbox_top.setMinWidth(978);
        hbox_top.setMaxWidth(978);
        hbox_top.setPrefWidth(978);
        hbox_top.setMaxHeight(50);
        hbox_top.setMinHeight(50);
        hbox_top.setPrefHeight(50);

        Label popup_title = new Label(title);
        popup_title.setStyle("-fx-font-size: 16; -fx-text-fill: #404040; -fx-font-weight: bold;");
        popup_title.setPadding(new Insets(0, 10, 0, 0));

        Image image = new Image(GenivisApplication.class.getResource("/com/opethic/genivis/ui/assets/close.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setStyle("-fx-cursor: hand;");
        imageView.setOnMouseClicked(event -> {
            primaryStage.close();
            OverlaysEffect.removeOverlaysEffect(stage);
        });
        HBox.setMargin(imageView, new Insets(10, 10, 0, 10));
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        // Set the font size
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox.setMargin(popup_title, new Insets(0, 0, 0, 10));

        hbox_top.getChildren().add(spacer);
        hbox_top.getChildren().add(imageView);
        hbox_top.setStyle("-fx-background-radius: 5 5 0 0; -fx-background-color: #D9F0FB; -fx-border-color: #C7C7CD; -fx-border-width: 0 0 2 0;");
        //BorderPane Under Center Layout.....................................................................................................
        HBox hbox_top1 = new HBox();
        hbox_top1.setMinWidth(978);
        hbox_top1.setMaxWidth(978);
        hbox_top1.setPrefWidth(978);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setMinHeight(50);
        hbox_top1.setMaxHeight(50);
        hbox_top1.setAlignment(Pos.TOP_LEFT);
        hbox_top1.setStyle("-fx-background-color: white;");
        TextField search = new TextField("");
        search.setPromptText("Search");
        search.setPadding(new Insets(10, 0, 0, 10));
        search.setPrefWidth(350);
        HBox.setMargin(search, new Insets(0, 0, 0, 10));
        hbox_top1.getChildren().addAll(search);
        vbox_top.setSpacing(10);
        vbox_top.getChildren().addAll(hbox_top, hbox_top1);
        Object[] selectedInvoice = new Object[2];
        Platform.runLater(() -> search.requestFocus());
        Integer initialIndex = -1;
        //BorderPane Under Bottom Layout..............................................................................................................
        VBox vBox = new VBox();

        TableView<TranxReturnsBillDTO> tableView = new TableView();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFocusTraversable(false);
        tableView.setPrefHeight(500);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        TableColumn<TranxReturnsBillDTO, String> tcBillsrNo = new TableColumn<>("Sr.no");
        TableColumn<TranxReturnsBillDTO, String> tcBillNumber = new TableColumn<>("Invoice Number");
        TableColumn<TranxReturnsBillDTO, String> tcBillAmt = new TableColumn<>("Invoice Amount");
        TableColumn<TranxReturnsBillDTO, String> tcBillDate = new TableColumn<>("Invoice Date");
        tableView.getColumns().addAll(tcBillsrNo, tcBillNumber, tcBillAmt, tcBillDate);
        tcBillsrNo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        tcBillNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNo()));
        tcBillAmt.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceAmount()));
        tcBillDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceDate()));
        fetchLedgerBillList(ledgerId, "srtn");
        tableView.setItems(observableLedgerBillList);
        vBox.getChildren().addAll(tableView);
        tableView.setItems(observableLedgerBillList);
        // Add a listener to the text property of the search TextField
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filter the items based on the newValue
            tableView.setItems(observableLedgerBillList.filtered(item ->
                    item.getInvoiceNo().toLowerCase().contains(newValue.toLowerCase()) ||
                            item.getInvoiceAmount().contains(newValue)
            ));
        });

        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if ((event.getCode().isModifierKey() || event.getCode().isLetterKey() ||
                        event.getCode().isDigitKey() || event.getCode() == KeyCode.BACK_SPACE)
                        && tableView.isFocused()) {
                    search.requestFocus();
                } else if (event.getCode() == KeyCode.DOWN && search.isFocused()) {
                    tableView.getSelectionModel().select(0);
                    tableView.requestFocus();
                } else if (event.isControlDown() && event.getCode() == KeyCode.L) {

                }
            }
        });
        borderPane.setTop(vbox_top);
        borderPane.setCenter(vBox);
        //Double click on ledger list
        tableView.setRowFactory(tv -> {
            TableRow<TranxReturnsBillDTO> row = new TableRow<>();
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2) {
                        TranxReturnsBillDTO tranxReturnsBillDTO = tableView.getSelectionModel().getSelectedItem();
                        selectedInvoice[0] = tranxReturnsBillDTO.getId();
                        selectedInvoice[1] = tranxReturnsBillDTO.getInvoiceNo();
                        callback.accept(selectedInvoice);
                        primaryStage.close();
                        OverlaysEffect.removeOverlaysEffect(stage);
                    }
                }
            });

            return row;
        });
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                TranxReturnsBillDTO tranxReturnsBillDTO = tableView.getSelectionModel().getSelectedItem();
                selectedInvoice[0] = tranxReturnsBillDTO.getId();
                selectedInvoice[1] = tranxReturnsBillDTO.getInvoiceNo();
                callback.accept(selectedInvoice);
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });
        Scene scene = new Scene(borderPane, 980, 610);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.setResizable(false);

        scene.setFill(Color.TRANSPARENT);

        primaryStage.centerOnScreen();

        primaryStage.show();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.close();
                OverlaysEffect.removeOverlaysEffect(stage);
            }
        });

    }
}



