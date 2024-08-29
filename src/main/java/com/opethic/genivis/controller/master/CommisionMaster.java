package com.opethic.genivis.controller.master;

import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.dto.CommissionMasterDTO;
import com.opethic.genivis.models.HeadType;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.AlertUtility;
import com.opethic.genivis.utils.CommonFunctionalUtils;
import com.opethic.genivis.utils.CommonValidationsUtils;
import com.opethic.genivis.utils.Globals;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.*;

public class CommisionMaster implements Initializable {

    @FXML
    private TableView<CommissionMasterDTO> tdvCreatecommissionMatertableview;

    @FXML
    private TableColumn<CommissionMasterDTO, String> col1CreatecommissionMatertbl, col2CreatecommissionMatertbl, col3CreatecommissionMatertbl;

    @FXML
    private ComboBox<HeadType> comboboxCreateCommissionMaster;

    @FXML
    private TextField tdCreatecommissionMaterIncentive, tdCreatecommissionMaterTDS;

    @FXML
    private Button btncommissionmastersubmit, btncommissionmasterclear;

    ObservableList<CommissionMasterDTO> orgRow = FXCollections.observableArrayList();

    private int id = 0;

    @FXML
    private BorderPane bpCommissionMasterRootPane;


    private static final Logger loggerCommission = LogManager.getLogger(CommisionMaster.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ResponsiveWiseCssPicker();

        bpCommissionMasterRootPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {


            if (event.getCode() == KeyCode.ENTER) {
                if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("submit")) {
                    System.out.println(targetButton.getText());
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("clear")) {
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
            }
            if (event.getCode() == KeyCode.S && event.isControlDown()) {
                btncommissionmastersubmit.fire();
            }
            if (event.getCode() == KeyCode.X && event.isControlDown()) {
                btncommissionmasterclear.fire();
            }

            if (event.getCode() == KeyCode.E && event.isControlDown()) {
//                btncommissionmasterclear.fire();
                setEditData();
            }




        });


//        comboboxCreateCommissionMaster.setOnKeyPressed(actionEvent -> {
////            if (actionEvent.getCode() == KeyCode.DOWN || actionEvent.getCode() == KeyCode.TAB) {
//            CommonValidationsUtils.comboBoxDataShow(comboboxCreateCommissionMaster);
////            }
//
//        });


        TextField[] textFields = {tdCreatecommissionMaterIncentive, tdCreatecommissionMaterTDS};
        for (TextField textField : textFields) {
            CommonFunctionalUtils.cursorMomentForTextField(textField);
        }

        setInputValidation();
        Platform.runLater(() -> {
            comboboxCreateCommissionMaster.requestFocus();

        });


        ObservableList<HeadType> headTypes = FXCollections.observableArrayList(getHeadTypeOption());
        comboboxCreateCommissionMaster.getItems().addAll(headTypes);
        comboboxCreateCommissionMaster.setConverter(new StringConverter<HeadType>() {
            @Override
            public String toString(HeadType headType) {
                return headType != null ? headType.getLabel() : "";
            }
            @Override
            public HeadType fromString(String string) {
                return null;
            }
        });
        getOnLoadData();
        initLoadTableFunctions();
        btncommissionmasterclear.setOnAction(e -> {
            ClearForm();
        });
        btncommissionmastersubmit.setOnAction(e ->
        {
            validateSubmitData();
        });

//        comboboxCreateCommissionMaster.setOnKeyPressed(this::handleEnterKeyPressed);


        comboboxCreateCommissionMaster.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.DOWN) {
                tdvCreatecommissionMatertableview.getSelectionModel().selectFirst();
            }
            if (actionEvent.getCode() == KeyCode.TAB) {
                CommonFunctionalUtils.cursorMomentForComboBox(comboboxCreateCommissionMaster);
            }

            CommonFunctionalUtils.comboBoxDataShow(comboboxCreateCommissionMaster);

        });
        tdCreatecommissionMaterIncentive.setOnKeyPressed(this::handleEnterKeyPressed);
        tdCreatecommissionMaterTDS.setOnKeyPressed(this::handleEnterKeyPressed);
        btncommissionmastersubmit.setOnKeyPressed(this::handleEnterKeyPressed);
        btncommissionmasterclear.setOnKeyPressed(this::handleEnterKeyPressed);
    }

    private void ResponsiveWiseCssPicker() {
        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);
        if (width >= 992 && width <= 1024) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle1.css").toExternalForm());
        } else if (width >= 1025 && width <= 1280) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle2.css").toExternalForm());
        } else if (width >= 1281 && width <= 1368) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle3.css").toExternalForm());
        } else if (width >= 1369 && width <= 1400) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle4.css").toExternalForm());
        } else if (width >= 1401 && width <= 1440) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle5.css").toExternalForm());
        } else if (width >= 1441 && width <= 1680) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle6.css").toExternalForm());
        } else if (width >= 1681 && width <= 1920) {
            bpCommissionMasterRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle7.css").toExternalForm());
        }
    }

        private void setEditData() {
                CommissionMasterDTO SelectedValue = tdvCreatecommissionMatertableview.getSelectionModel().getSelectedItem();
                if (SelectedValue != null) {
                    id = SelectedValue.getId();
                    HeadType selectedRole = findByRoleString(SelectedValue.getRoleType());
                    comboboxCreateCommissionMaster.setValue(selectedRole);
                    tdCreatecommissionMaterIncentive.setText(SelectedValue.getProductLevel());
                    tdCreatecommissionMaterTDS.setText(SelectedValue.getTdsPer() + "");
                    btncommissionmastersubmit.setText("Update");
                    setFocus();
                } else {
                    String msg = "Record Not Found!";
                    AlertUtility.CustomCallback callback = number -> {
                        setFocus();
                    };
                    AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
                }


        }




    private void handleEnterKeyPressed(KeyEvent keyEvent) {
        Node source = (Node) keyEvent.getSource();

        if(keyEvent.getCode() == KeyCode.DOWN){
            tdvCreatecommissionMatertableview.requestFocus();
        }

        if (keyEvent.getCode() == KeyCode.ENTER) {
            switch (source.getId()) {
                case "comboboxCreateCommissionMaster":
                        Platform.runLater(() -> {
                            tdCreatecommissionMaterIncentive.requestFocus();
                        });
                    break;
                case "tdCreatecommissionMaterIncentive":
                        Platform.runLater(() -> {
                            tdCreatecommissionMaterTDS.requestFocus();
                        });
                    break;
                case "tdCreatecommissionMaterTDS":
                        Platform.runLater(() -> {
                            btncommissionmastersubmit.requestFocus();
                        });
                    break;
                case "btncommissionmastersubmit":
                    validateSubmitData();
                    break;
                case "btncommissionmasterclear":
                    ClearForm();
                    break;
                default:
                    break;
            }
        }else if (keyEvent.getCode() == KeyCode.TAB) {
            switch (source.getId()) {
                case "btncommissionmastersubmit":
                    btncommissionmasterclear.requestFocus();
                    break;
                default:
                    break;
            }
        }
    }

    private void setInputValidation() {
        TextFormatter<String> inFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*\\.?[0-9]*")) {
                return change;
            }
            return null;
        });
        tdCreatecommissionMaterIncentive.setTextFormatter(inFormatter);
        tdCreatecommissionMaterTDS.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*\\.?[0-9]*")) {
                return change;
            }
            return null;
        }));
    }

    private void setFocus() {
        comboboxCreateCommissionMaster.requestFocus();
    }

    public void getOnLoadData() {
        APIClient apiClient=null;
        try {
            apiClient=new APIClient(EndPoints.GetAllCommissionMaster, "", RequestType.GET);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JSONObject jsonObject = new JSONObject(workerStateEvent.getSource().getValue().toString());
                    if (jsonObject.getInt("responseStatus") == 200) {
                        JSONArray jarr = jsonObject.getJSONArray("responseObject");
                        orgRow.clear();
                        if (jarr.length() > 0) {
                            for (Object object : jarr) {
                                JSONObject obj = new JSONObject(object.toString());
                                String roleType = obj.getString("roleType");
                                StringBuilder sb = new StringBuilder(roleType);
                                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                                String roleTypeCapitalized = sb.toString();
                                String finRoleType = roleTypeCapitalized + " Head";
                                orgRow.add(new CommissionMasterDTO(obj.getDouble("tdsPer"), obj.getString("productLevel"), obj.getInt("id"),finRoleType));
                            }
                            FilteredList<CommissionMasterDTO> filteredList = new FilteredList<>(orgRow);
                            col1CreatecommissionMatertbl.setCellValueFactory(new PropertyValueFactory<>("roleType"));
                            col2CreatecommissionMatertbl.setCellValueFactory(new PropertyValueFactory<>("productLevel"));
                            col3CreatecommissionMatertbl.setCellValueFactory(new PropertyValueFactory<>("tdsPer"));
                            tdvCreatecommissionMatertableview.getItems().clear();
                            tdvCreatecommissionMatertableview.getItems().addAll(filteredList);
                        }
                    } else {
                        Stage stage1 = (Stage) bpCommissionMasterRootPane.getScene().getWindow();
                        String msg = jsonObject.getString("message");
                        AlertUtility.CustomCallback callback = number -> {
                            setFocus();
                        };
                        AlertUtility.AlertError(stage1,AlertUtility.alertTypeError, msg, callback);
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API cancelled in getOnLoadData() Commission Master" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API failed in getOnLoadData() Commission Master" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            loggerCommission.error("Exception getOnLoadData() Commission Master : " + Globals.getExceptionString(e));
        }
    }


    private void CreateNewData(String roleType, String productLevel, String tdsPer) {
        APIClient apiClient=null;
        try {
            Boolean isCreateFlag = ValidateBeforeAddingNewData(roleType);
            if (isCreateFlag == true) {
                Map<String, String> map = new HashMap<>();
                map.put("roleType", roleType);
                map.put("productLevel", productLevel);
                map.put("tds_per", tdsPer);
                String formData = Globals.mapToStringforFormData(map);
                apiClient = new APIClient(EndPoints.CreateCommissionMaster, formData, RequestType.FORM_DATA);
                apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        JSONObject jsonObject = new JSONObject(workerStateEvent.getSource().getValue().toString());
                        if (jsonObject.getInt("responseStatus") == 200) {
                            getOnLoadData();
                            ClearForm();
                        } else {
                            Stage stage1 = (Stage) bpCommissionMasterRootPane.getScene().getWindow();
                            String msg = jsonObject.getString("message");
                            AlertUtility.CustomCallback callback = number -> {
                                setFocus();
                            };
                            AlertUtility.AlertError(stage1,AlertUtility.alertTypeError, msg, callback);
                        }
                    }
                });
            } else {
                String msg = "Record Is Already Present !";
                AlertUtility.CustomCallback callback = number -> {
                    setFocus();
                };
                AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
                ClearForm();
            }
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API cancelled in CreateNewData() Commission Master" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API failed in CreateNewData() Commission Master" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            loggerCommission.error("Exception CreateNewData() Commission Master: " + Globals.getExceptionString(e));
        }
    }

    private Boolean ValidateBeforeAddingNewData(String roleType) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("roleType", roleType);
            String formData = Globals.mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.validateCommissionMaster);
            JSONObject jsonObject = new JSONObject(response.body());
            if (jsonObject.getInt("responseStatus") == 200) {
                return true;
            } else {
                String msg = jsonObject.getString("message");
                AlertUtility.CustomCallback callback = number -> {
                    setFocus();
                };
                AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
                return false;
            }
        } catch (Exception e) {
            loggerCommission.error("Exception ValidateBeforeAddingNewData() Commission Master : " + Globals.getExceptionString(e));
        }
        return false;
    }

    private void ClearForm() {
        id = 0;

        comboboxCreateCommissionMaster.getSelectionModel().clearSelection();
        tdCreatecommissionMaterIncentive.clear();
        tdCreatecommissionMaterTDS.clear();
        btncommissionmastersubmit.setText("Submit");
        setFocus();
    }

    private void initLoadTableFunctions() {
        tdvCreatecommissionMatertableview.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                CommissionMasterDTO SelectedValue = tdvCreatecommissionMatertableview.getSelectionModel().getSelectedItem();
                if (SelectedValue != null) {
                    id = SelectedValue.getId();
                    HeadType selectedRole = findByRoleString(SelectedValue.getRoleType());
                    comboboxCreateCommissionMaster.setValue(selectedRole);
                    tdCreatecommissionMaterIncentive.setText(SelectedValue.getProductLevel());
                    tdCreatecommissionMaterTDS.setText(SelectedValue.getTdsPer() + "");
                    btncommissionmastersubmit.setText("Update");
                    setFocus();
                } else {
                    String msg = "Record Not Found!";
                    AlertUtility.CustomCallback callback = number -> {
                        setFocus();
                    };
                    AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
                }
            }
        });

        tdvCreatecommissionMatertableview.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                CommissionMasterDTO SelectedValue = tdvCreatecommissionMatertableview.getSelectionModel().getSelectedItem();
                if (SelectedValue != null) {
                    id = SelectedValue.getId();
                    HeadType selectedRole = findByRoleString(SelectedValue.getRoleType());
                    comboboxCreateCommissionMaster.setValue(selectedRole);
                    tdCreatecommissionMaterIncentive.setText(SelectedValue.getProductLevel());
                    tdCreatecommissionMaterTDS.setText(SelectedValue.getTdsPer() + "");
                    btncommissionmastersubmit.setText("Update");
                    setFocus();
                } else {
                    String msg = "Record Not Found!";
                    AlertUtility.CustomCallback callback = number -> {
                        setFocus();
                    };
                    AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
                }
            }
        });
    }

    private List<HeadType> getHeadTypeOption() {
        List<HeadType> headTypeList = new ArrayList<>();
        headTypeList.add(new HeadType("state", "State Head"));
        headTypeList.add(new HeadType("zonal", "Zonal Head"));
        headTypeList.add(new HeadType("region", "Region Head"));
        headTypeList.add(new HeadType("district", "District Head"));
        return headTypeList;
    }

    private HeadType findByRoleString(String role) {
        List<HeadType> headTypeList = getHeadTypeOption();
        return headTypeList.stream().filter(slug -> slug.getLabel().equalsIgnoreCase(role)).findAny().orElse(null);
    }

    private void UpdateExistingData(int id, String roleType, String productLevel, String tdsPer) {
        APIClient apiClient=null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", id + "");
            map.put("roleType", roleType);
            map.put("productLevel", productLevel);
            map.put("tds_per", tdsPer);
            String formData = Globals.mapToStringforFormData(map);
            apiClient = new APIClient(EndPoints.UpdateCommissionMaster, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JSONObject jsonObject = new JSONObject(workerStateEvent.getSource().getValue().toString());
                    if (jsonObject.getInt("responseStatus") == 200) {
                        Stage stage1 = (Stage) bpCommissionMasterRootPane.getScene().getWindow();
                        String msg = jsonObject.getString("message");
                        AlertUtility.CustomCallback callback = number -> {
                            if (number == 1) {
                                setFocus();
                            }
                        };
                        AlertUtility.AlertSuccess(stage1,AlertUtility.alertTypeSuccess, msg, callback);
                        getOnLoadData();
                        ClearForm();
                    } else {
                        Stage stage2 = (Stage) bpCommissionMasterRootPane.getScene().getWindow();
                        String msg = jsonObject.getString("message");
                        AlertUtility.CustomCallback callback = number -> {
                            setFocus();
                        };
                        AlertUtility.AlertError(stage2,AlertUtility.alertTypeError, msg, callback);
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API cancelled in UpdateExistingData() Commission Master" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    loggerCommission.error("Network API failed in UpdateExistingData() Commission Master" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            loggerCommission.error("Exception UpdateExistingData() : " + Globals.getExceptionString(e));
        }
    }


    private void validateSubmitData() {
        try {
            if (comboboxCreateCommissionMaster.getSelectionModel().getSelectedItem() != null && !tdCreatecommissionMaterIncentive.getText().isEmpty() && !tdCreatecommissionMaterTDS.getText().isEmpty()) {
                if (id > 0) {
                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Update ?", input -> {
                        if (input == 1) {
                            UpdateExistingData(id, comboboxCreateCommissionMaster.getSelectionModel().getSelectedItem().getValue(), tdCreatecommissionMaterIncentive.getText(), tdCreatecommissionMaterTDS.getText());
                        }
                    });
                } else {
                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Submit ?", input -> {
                        if (input == 1) {
                            CreateNewData(comboboxCreateCommissionMaster.getSelectionModel().getSelectedItem().getValue(), tdCreatecommissionMaterIncentive.getText(), tdCreatecommissionMaterTDS.getText());
                        }
                    });

                }
            } else {
                String msg = "Please Fill Required Fields!";
                AlertUtility.CustomCallback callback = number -> {
                    setFocus();
                };
                AlertUtility.AlertError(AlertUtility.alertTypeError, msg, callback);
            }
        } catch (Exception e) {
            loggerCommission.error("Exception validateSubmitData() : " + Globals.getExceptionString(e));
        }
    }
}
