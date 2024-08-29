package com.opethic.genivis.controller.master;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.dto.AreaHeadListDTO;
import com.opethic.genivis.dto.CourierServiceDTO;
import com.opethic.genivis.dto.DoctorMasterDTO;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.utils.AlertUtility;
import com.opethic.genivis.utils.CommonValidationsUtils;
import com.opethic.genivis.utils.GlobalController;
import com.opethic.genivis.utils.Globals;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.AREA_HEAD_LIST_SLUG;

public class CourierServiceController implements Initializable {

//    @FXML
//    private TableColumn<PatientDTO,String> tfCourierServiceServiceName;

    @FXML
    private VBox boxV;
    @FXML
    private BorderPane spRootCourierServicePane;
    @FXML
    private TextField tfCourierServiceServiceName;
    @FXML
    private TextField tfCourierServiceContactPerson;
    @FXML
    private TextField tfCourierServiceContactNo;
    @FXML
    private TextField tfCourierServiceServiceAddress;
    @FXML
    private TextField fieldSearch;


    @FXML
    private TableView<CourierServiceDTO> tcCourierServiceView;


    @FXML
    private TableColumn<CourierServiceDTO, String> tcServiceName;
    @FXML
    private TableColumn<CourierServiceDTO, String> tcContactPerson;
    @FXML
    private TableColumn<CourierServiceDTO, String> tcContactNo;
    @FXML
    private TableColumn<CourierServiceDTO, String> tcServiceAddress;


    public String id = "";
    String message = "";


    @FXML
    private Button btnCourierServiceServiceCreateSubmit;
    @FXML
    private Button btnCourierServiceServiceCreateCancel;

    private ObservableList<CourierServiceDTO> originalData;


    private Node[] focusableNodes;
    private HttpResponse<String> response;


    // Method to check the field and highlight if necessary

    private boolean matchesKeyword(CourierServiceDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();

        // Check if any of the columns contain the keyword
        return item.getService_name().toLowerCase().contains(lowerCaseKeyword) ||
                item.getContact_person().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCourierContactNo().toLowerCase().contains(lowerCaseKeyword);
//                item.getState().toLowerCase().contains(lowerCaseKeyword) ||
//                item.getAreaRole().toLowerCase().contains(lowerCaseKeyword);
    }

    private void filterData(String keyword) {
        ObservableList<CourierServiceDTO> filteredData = FXCollections.observableArrayList();

        if (keyword.isEmpty()) {
            tcCourierServiceView.setItems(originalData);
            return;
        }

        for (CourierServiceDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tcCourierServiceView.setItems(filteredData);
    }

    private void nodetraversal(Node current_node, Node next_node) {
        current_node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                next_node.requestFocus();
                event.consume();
            }

            if (event.getCode() == KeyCode.ENTER) {
                if (current_node instanceof Button button) {
                    button.fire();
                }
            } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
                if (current_node instanceof RadioButton radioButton) {
                    radioButton.setSelected(!radioButton.isSelected());
                    radioButton.fire();
                }
            }
        });
    }


    private void cursorMoment(TextField textField) {

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String text = textField.getText().trim();
                if (text.isEmpty()) {
                    textField.requestFocus();
                }
            }
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ResponsiveWiseCssPicker();
//        tfCourierServiceServiceName.setText("");
//            tfCourierServiceServiceName.setOnKeyPressed(event -> {
//            if (event.getCode()==KeyCode.ENTER || event.getCode()==KeyCode.SPACE || event.getCode()==KeyCode.TAB ){
////                cursorMoment(tfCourierServiceServiceName);
//                if (tfCourierServiceServiceName.getText().isEmpty()) {
//                    tfCourierServiceServiceName.requestFocus();
//                }
//            }
////            if (event.getCode() == KeyCode.DOWN) {
////                fieldSearch.requestFocus();
////            }
//        });

//        CommonValidationsUtils.changeStarColour(boxV);
        CommonValidationsUtils.restrictMobileNumber(tfCourierServiceContactNo);


        spRootCourierServicePane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {


            if (event.getCode() == KeyCode.ENTER) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB && tfCourierServiceServiceName.isFocused()) {
//                cursorMoment(tfCourierServiceServiceName);
                    if (tfCourierServiceServiceName.getText().isEmpty()) {
                        tfCourierServiceServiceName.requestFocus();
                    }
                }
                if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("submit")) {
                    System.out.println(targetButton.getText());
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("Clear")) {
                    System.out.println(targetButton.getText());
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("update")) {
                    System.out.println(targetButton.getText());
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
            } else if (event.getCode() == KeyCode.DOWN && fieldSearch.isFocused()) {
                tcCourierServiceView.getSelectionModel().select(0);
                tcCourierServiceView.requestFocus();
            } else if (event.getCode() == KeyCode.DOWN && tfCourierServiceServiceName.isFocused()) {
                fieldSearch.requestFocus();
            } else if (event.getCode() == KeyCode.ENTER && tfCourierServiceServiceName.isFocused()) {
//                cursorMoment(tfCourierServiceServiceName);
                if (tfCourierServiceServiceName.getText().isEmpty()) {
                    tfCourierServiceServiceName.requestFocus();
                }
            }

//            tfCourierServiceServiceName.setOnKeyPressed(event->{
//
//            });


            if (event.getCode() == KeyCode.S && event.isControlDown()) {
                btnCourierServiceServiceCreateSubmit.fire();
            }
            if (event.getCode() == KeyCode.X && event.isControlDown()) {
                btnCourierServiceServiceCreateCancel.fire();
            }
            if (event.getCode() == KeyCode.E && event.isControlDown()) {
                setEditData();
            }
        });


        tcServiceName.prefWidthProperty().bind(tcCourierServiceView.widthProperty().multiply(0.2));
        tcContactPerson.prefWidthProperty().bind(tcCourierServiceView.widthProperty().multiply(0.2));
        tcContactNo.prefWidthProperty().bind(tcCourierServiceView.widthProperty().multiply(0.2));
        tcServiceAddress.prefWidthProperty().bind(tcCourierServiceView.widthProperty().multiply(0.3));

        getCourierService();

        btnCourierServiceServiceCreateSubmit.setOnAction(e -> {
            if (CommonValidationsUtils.validateForm(tfCourierServiceServiceName, tfCourierServiceContactPerson, tfCourierServiceContactNo, tfCourierServiceServiceAddress)) {
                createCourierService();
            }
        });

        TextField[] textFields = {tfCourierServiceContactPerson, tfCourierServiceContactNo, tfCourierServiceServiceAddress};
        for (TextField textField : textFields) {
            cursorMoment(textField);
        }


        originalData = tcCourierServiceView.getItems();
        fieldSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        tcCourierServiceView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                setEditData();
            }
        });

        tcCourierServiceView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                setEditData();
            }

            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                fieldSearch.requestFocus();
            }

        });


        // Add listener for focus change to restrict Mobile Number and Email when cursor moves away
//        CommonValidationsUtils.restrictEmail(tfCmpAdmEmail);

        Platform.runLater(() -> tfCourierServiceServiceName.requestFocus());


        tfCourierServiceContactNo.setOnKeyPressed(event -> {
            System.out.println(event);
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                validateDuplication();
                System.out.println(validateDuplication());
            }
        });

    }

    private void ResponsiveWiseCssPicker() {
        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);
        if (width >= 992 && width <= 1024) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle1.css").toExternalForm());
        } else if (width >= 1025 && width <= 1280) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle2.css").toExternalForm());
        } else if (width >= 1281 && width <= 1368) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle3.css").toExternalForm());
        } else if (width >= 1369 && width <= 1400) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle4.css").toExternalForm());
        } else if (width >= 1401 && width <= 1440) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle5.css").toExternalForm());
        } else if (width >= 1441 && width <= 1680) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle6.css").toExternalForm());
        } else if (width >= 1681 && width <= 1920) {
            spRootCourierServicePane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle7.css").toExternalForm());
        }
    }

    private void showAlert(String message) {
        //TODO: use util for all Alert messages
        Alert alert = new Alert(Alert.AlertType.NONE, message, ButtonType.OK);
        Duration duration = Duration.seconds(0.9);
        alert.show();
        Timeline timeline = new Timeline(new KeyFrame(duration, event -> alert.close()));
        timeline.play();
    }


    public void setEditData() {
        CourierServiceDTO selectedItem = tcCourierServiceView.getSelectionModel().getSelectedItem();
        System.out.println("CourierServiceDTO-->" + selectedItem.getId());
        id = selectedItem.getId();
        if (selectedItem != null) {

            tfCourierServiceServiceName.setText(selectedItem.getService_name());
            tfCourierServiceContactPerson.setText(selectedItem.getContact_person());
            tfCourierServiceContactNo.setText(selectedItem.getCourierContactNo());
            tfCourierServiceServiceAddress.setText(selectedItem.getCourierServiceAddress());

            btnCourierServiceServiceCreateSubmit.setText("Update");
            tfCourierServiceServiceName.requestFocus();

        }
    }

    public void cancelCourierService() {

        AlertUtility.CustomCallback callback = number -> {
            if (number == 1) {
                id = "";
                tfCourierServiceServiceName.setText("");
                tfCourierServiceContactNo.setText("");
                tfCourierServiceContactPerson.setText("");
                tfCourierServiceServiceAddress.setText("");
                btnCourierServiceServiceCreateSubmit.setText("Submit");
            } else {
                System.out.println("working!");
            }
        };
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do You Want To Clear", callback);


    }

    private boolean validateDuplication() {
        // Get all items from the TableView
        ObservableList<CourierServiceDTO> items = tcCourierServiceView.getItems();

        // Iterate over the items and compare
        for (int i = 0; i < items.size(); i++) {
            CourierServiceDTO item1 = items.get(i);

            // Perform your comparison logic here
            System.out.println(!id.matches(String.valueOf(item1.getId())));
            boolean cndn = item1.getCourierContactNo().equals(tfCourierServiceContactNo.getText());
            // Perform your comparison logic here
            if (id.matches(String.valueOf(item1.getId()))) {
                System.out.println("not a duplicate");
            } else {
                if (cndn) {
                    // Handle duplication case
                    tfCourierServiceContactNo.requestFocus();

                    System.out.println("Duplicate item found: " + item1);
                    //                    tfCourierServiceContactNo.requestFocus();
                    AlertUtility.AlertErrorTimeout("Error", "Duplicate Record", callback -> {
                        tfCourierServiceContactNo.requestFocus();
                    });
                    return true;
                }
            }
        }

        return false;  // No duplicate found
    }


    public void createCourierService() {
//        CommonValidationsUtils.restrictMobileNumber(tfCourierServiceContactNo);
        if (!validateDuplication()) {

            String courierName = tfCourierServiceServiceName.getText();
            String courierContactNo = tfCourierServiceContactNo.getText();
            String courierContactPerson = tfCourierServiceContactPerson.getText();
            String courierServiceAddress = tfCourierServiceServiceAddress.getText();

            if (!courierName.isBlank() && !courierContactNo.isBlank() && !courierContactPerson.isBlank() && !courierServiceAddress.isBlank()) {
                System.out.println("courierName: " + courierName);

                Map<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("service_name", courierName);
                map.put("contact_person", courierContactPerson);
                map.put("contact_No", courierContactNo);
                map.put("service_Add", courierServiceAddress);

                String formData = Globals.mapToStringforFormData(map);
                System.out.println("formData: " + formData);

                AlertUtility.CustomCallback callback = number -> {
                    if (number == 1) {
                        HttpResponse<String> response;
                        if (id.isEmpty()) {
                            response = APIClient.postFormDataRequest(formData, "create_courier_master");
                        } else {
                            response = APIClient.postFormDataRequest(formData, "update_courier_master");
                        }

                        if (response != null) {
                            JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                            System.out.println("Response => " + responseBody);
                            String message = responseBody.get("message").getAsString();

                            if (responseBody.get("responseStatus").getAsInt() == 200) {
                                clearCourierServiceFields();

//
                                AlertUtility.CustomCallback callbackq = numberq -> {
                                    if (numberq == 1) {
                                        System.out.println("done");


                                        getCourierService();
                                        tfCourierServiceServiceName.requestFocus();
                                        if (id != null && !id.isEmpty()) {
                                            // Parse the ID to an integer
                                            String targetId = id;
                                            // Find the row index based on the ID
                                            ObservableList<CourierServiceDTO> items = tcCourierServiceView.getItems();
                                            int targetIndex = -1;

                                            for (int i = 0; i < items.size(); i++) {
                                                CourierServiceDTO item = items.get(i);
                                                if (item.getId().contains(targetId)) { // Replace getId() with your actual ID getter
                                                    targetIndex = i;
                                                    break;
                                                }
                                            }
                                            // If a matching row is found, select it
                                            if (targetIndex != -1) {
                                                tcCourierServiceView.getSelectionModel().select(targetIndex);
                                            } else {
                                                // Handle the case where the ID is not found, if necessary
                                                System.out.println("ID not found in the table");
                                            }
                                        } else {
                                            int lastIndex = tcCourierServiceView.getItems().size() - 1;
                                            if (lastIndex >= 0) {
                                                tcCourierServiceView.getSelectionModel().select(lastIndex);
                                            }
                                        }

                                        btnCourierServiceServiceCreateSubmit.setText("Submit");

                                        id = "";

//                                    response = APIClient.postMultipartRequest(map, finalFileMap2, EndPoints.updateAreaHead, headers);
//                                    GlobalController.getInstance().addTabStatic(AREA_HEAD_LIST_SLUG, false);
                                    } else {
                                        System.out.println("working!");
                                    }
                                };
                                AlertUtility.AlertSuccessTimeout(AlertUtility.alertTypeConfirmation, responseBody.get("message").getAsString(), callbackq);


                                getCourierService();
                            }
                        }
                    } else {
                        System.out.println("working!");
                    }
                };
                if (id != "") {

                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure you want to update?", callback);
                } else {

                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure you want to submit?", callback);
                }

            }
        }
    }

    private void clearCourierServiceFields() {
        tfCourierServiceContactPerson.setText("");
        tfCourierServiceServiceAddress.setText("");
        tfCourierServiceServiceName.setText("");
        tfCourierServiceContactNo.setText("");
    }

    public void getCourierService() {
        try {
            tcCourierServiceView.getItems().clear();
            HttpResponse<String> response = APIClient.getRequest("get_all_courier_master");
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println(jsonObject);
            ObservableList<CourierServiceDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("responseObject");


                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String serviceName = item.get("service_name").getAsString();
                        String courierContactNo = item.get("contact_No").getAsString();
                        String courierContactPerson = item.get("contact_person").getAsString();
                        String courierServiceAddress = item.get("service_Add").getAsString();


                        observableList.add(new CourierServiceDTO(id, serviceName,
                                courierContactNo, courierContactPerson, courierServiceAddress)
                        );
                    }


                    tcServiceName.setCellValueFactory(new PropertyValueFactory<>("service_name"));
                    tcContactNo.setCellValueFactory(new PropertyValueFactory<>("contact_No"));

                    tcContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact_person"));
                    tcServiceAddress.setCellValueFactory(new PropertyValueFactory<>("service_Add"));
                    tcCourierServiceView.setItems(observableList);

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


}
