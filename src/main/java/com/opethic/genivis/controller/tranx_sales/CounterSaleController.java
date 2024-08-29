package com.opethic.genivis.controller.tranx_sales;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.controller.commons.OverlaysEffect;
import com.opethic.genivis.controller.dialogs.BatchWindow;
import com.opethic.genivis.controller.dialogs.SingleInputDialogs;
import com.opethic.genivis.controller.master.ProductListController;
import com.opethic.genivis.controller.tranx_purchase.*;
import com.opethic.genivis.controller.tranx_sales.common.TranxCommonPopUps;
import com.opethic.genivis.controller.tranx_sales.invoice.TranxSalesInvoiceCreate;
import com.opethic.genivis.dto.*;
import com.opethic.genivis.controller.commons.SwitchButton;
import com.opethic.genivis.dto.CmpTRowDTO;
import com.opethic.genivis.dto.PatientDTO;
import com.opethic.genivis.dto.TranxBatchWindowDTO;
import com.opethic.genivis.dto.TranxProductWindowDTO;
import com.opethic.genivis.dto.cmp_t_row.BatchWindowTableDTO;
import com.opethic.genivis.dto.counter.CSToSInvDTO;
import com.opethic.genivis.dto.counter.CSToSInvRowDTO;
import com.opethic.genivis.dto.pur_invoice.*;
import com.opethic.genivis.dto.pur_invoice.reqres.RowListDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.dto.reqres.product.TableCellCallback;
import com.opethic.genivis.dto.reqres.pur_tranx.*;
import com.opethic.genivis.dto.reqres.sales_tranx.*;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.sql.SQLTransactionRollbackException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.opethic.genivis.network.EndPoints.*;
import static com.opethic.genivis.utils.FxmFileConstants.SALES_INVOICE_CREATE_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.SALES_ORDER_CREATE_SLUG;
import static com.opethic.genivis.utils.Globals.decimalFormat;
import static com.opethic.genivis.utils.Globals.mapToStringforFormData;

public class CounterSaleController implements Initializable {


    @FXML
    private Button addRowInCounterSale, btnCounterSaleBatWinClose, btnCounterSaleSubmit, btnPatientWindowClose;

    @FXML
    private TableView<CounterSaleRowDTO> tblvCounterSaleView;
    @FXML
    private TableColumn<CounterSaleRowDTO, String> tblcCounterSaleSrNo, tblcCounterSaleParticular, tblcCounterSalePackage, tblcCounterSaleUnit, tblcCounterSaleBatchNo, tblcCounterSaleQty, tblcCounterSaleRate,
            tblcCounterSaleDIscPer, tblcCounterSaleNetAmt, tblcCounterSaleAction, tblcCounterSaleLevelA, tblcCounterSaleLevelB, tblcCounterSaleLevelC;
    @FXML
    private HBox hbapCounterSaleProductWindow, hbapCounterSaleBatchWindow, hbapConsumerSalePatientWindow;
    @FXML
    private TableView tblvPurChallanPrdWindtableview, tblvConsumerSalePatientWindtableview;
    @FXML
    private TableColumn tblcCounterSalePrdWindCode, tblcCounterSalePrdWindPrdName, tblcCounterSalePrdWindPacking, tblcCounterSalePrdWindBarcode, tblcCounterSalePrdWindBrand,
            tblcCounterSalePrdWindMrp, tblcCounterSalePrdWindCurrStk, tblcCounterSalePrdWindUnit, tblcCounterSalePrdWindSaleRate, tblcCounterSalePrdWindAction;
    @FXML
    private Button btnCounterSalePrdWinAddProduct, btnCounterSalePrdWindowClose, btnCounterSaleToSaleInvoice, btnConsumerPrescription;

    @FXML
    private TableView tblvCounterSaleBatWinCreTableview;
    @FXML
    private TableColumn tblcCounterSaleBatWinBatchNo, tblcCounterSaleBatWinPurDate, tblcCounterSaleBatWinMFGDate, tblcCounterSaleBatWinEXPDate, tblcCounterSaleBatWinMRP, tblcCounterSaleBatWinPurRate,
            tblcCounterSaleBatWinQty, tblcCounterSaleBatWinFreeQty, tblcCounterSaleBatWinDisPer, tblcCounterSaleBatWinDisAmt, tblcCounterSaleBatWinBarcode, tblcCounterSaleBatWinMarginPer,
            tblcCounterSaleBatWinFSR, tblcCounterSaleBatWinCSR, tblcCounterSaleBatWinSaleRate, tblcCounterSaleBatWinAction;

    @FXML
    private TableView tblvCounterSaleList;

    @FXML
    private TableColumn<CounterSaleDTO, String> tblcCounterSaleListInvNo, tblcCounterSaleListInvDate, tblcCounterSaleListMobile, tblcCounterSaleListQty, tblcCounterSaleListBillAmt, tblcCounterSaleListPayMode, tblcCounterSaleListProduct,
            tblcCounterSaleListPackage, tblcCounterSaleListUnit, tblcCounterSaleListBatch, tblcCounterSaleListRate, tblcCounterSaleListDis;
    @FXML
    private TableColumn<CounterSaleDTO, Boolean> tblcCounterSaleSelect;

    @FXML
    private TableView tblcConsumerSaleList;

    @FXML
    private TableColumn tblcConsumerSaleListInvNo, tblcConsumerSaleListInvDate, tblcConsumerSaleListClientName, tblcConsumerSaleListClientAddress, tblcConsumerSaleListMobile,
            tblcConsumerSaleListQty, tblcConsumerSaleListBillAmt, tblcConsumerSaleListSelect, tblcConsumerSaleListPayMod;


    @FXML
    private Label lblCounterSaleBatWinPrdName, lblCounterSaleBatWinCWT, lblCounterSaleBatWinCWOT, lblCounterSaleBatWinTax;
    @FXML
    private TextField tfCounterSaleBatWinBatSearch;
    @FXML
    private Label lblCounterSaleGrossTotal, lblCounterSaleDis, lblCounterSaleTotal, lblCounterSaleTax, lblCounterSaleBillAmt;

    @FXML
    private RadioButton rbPaymentModeCash, rbPaymentModeMulti, rbCounterSaleInvoice, rbCounterSaleProducts, rbCounterSalePMAll, rbCounterSalePMCash, rbCounterSalePMMulti;

    @FXML
    private TextField tfCounterCreateMobile, tfConsumerCName, tfConsumerCAddress, tfConsumerDoctorName, tfConsumerDoctorAddress, tfConsumerPrescription, tfCounterSaleDiscPer;
    ToggleGroup rbPaymentMode = new ToggleGroup();

    private static ObservableList<String> unitList = FXCollections.observableArrayList();

    private String productId = "";
    public String CounterToSaleInvId = "";

    private String responseBody;
    private int selectedRowIndex;
    private static int rowIndexParticular;
    private static int rowIndexBatchNo;
    private String prdBatchTaxPer;
    private String selectedRowPrdId;
    private String productIdSelected;

    private int rowIndexSelected;
    private String selectedValue = "", id = "", message = "", patientId = "", doctorId = "", counterId = "", consumerId = "", radioPaymentMode = "all", radioDataType = "invoice";


    private Boolean isConsumer = false;
    @FXML
    private SwitchButton switchConsumer;
    @FXML
    private VBox vboxConsumer;

    @FXML
    private HBox hbapCounterSalePatientMaster, hbapCounterSaleDoctorName;

    @FXML
    private TableView tblvCounterSalePatientMaster;

    @FXML
    private TableColumn tblcCounterSalePatMasCode, tblcCounterSalePatMasPatientName, tblcCounterSalePatMasMobileNo, tblcCounterSalePatMasAddress;

    @FXML
    private TableView tblvCounterSaleDoctor;

    @FXML
    private TableColumn tblcCounterSaleDoctorName;

    @FXML
    private Button btnCounterSaleDoctorClose, btnCounterSalePatientMasterClose, btnCounterSaleAddPatient, btnCounterSaleDoctorAdd;


    @FXML
    private Label lblConsumerCName, lblConsumerCAddress, lblConsumerDoctorName, lblConsumerDoctorAddress, lblConsumerPrescription;
    @FXML

    private ScrollPane spCounterSaleRootPane;
    private JsonObject jsonObject = null;
    private static final Logger logger = LogManager.getLogger(CounterSaleController.class);

    private ToggleGroup toggleGroupPayMode = new ToggleGroup();
    private ToggleGroup toggleGroupDatType = new ToggleGroup();
    private ToggleGroup toggleGroup = new ToggleGroup();

    private int Selected_index = 0;
    private ObservableList<CounterSaleDTO> originalData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        counterSaleListTableDesign();
        consumerSaleListTableDesign();

        //todo : autofocus on Search
        Platform.runLater(() -> tfCounterCreateMobile.requestFocus());
        //         Enter traversal
        spCounterSaleRootPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
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
        });

        tableInitiliazation();

        //settlement pop up window


        // Creating the Toggle Group
        rbPaymentModeCash.setToggleGroup(toggleGroupPayMode);
        rbPaymentModeMulti.setToggleGroup(toggleGroupPayMode);

        // Creating the Toggle Group
        rbCounterSaleInvoice.setToggleGroup(toggleGroupDatType);
        rbCounterSaleProducts.setToggleGroup(toggleGroupDatType);

        // Creating the Toggle Group
        rbCounterSalePMAll.setToggleGroup(toggleGroup);
        rbCounterSalePMCash.setToggleGroup(toggleGroup);
        rbCounterSalePMMulti.setToggleGroup(toggleGroup);

        responsiveCmpt();
        switchConsumer.setParentBox(vboxConsumer);

//        commmon


        switchConsumer.switchOnProperty().addListener((observable, oldValue, newValue) -> {
            isConsumer = newValue;
            if (newValue == false) {
                System.out.println("Helllo >> ");
                tfConsumerCName.setVisible(false);
                tfConsumerCAddress.setVisible(false);
                tfConsumerDoctorName.setVisible(false);
                tfConsumerDoctorAddress.setVisible(false);
//                tfConsumerPrescription.setVisible(false);
                tfCounterSaleDiscPer.setVisible(false);
                lblConsumerCName.setVisible(false);
                lblConsumerCAddress.setVisible(false);
                lblConsumerDoctorName.setVisible(false);
                lblConsumerDoctorAddress.setVisible(false);
                lblConsumerPrescription.setVisible(false);
                btnConsumerPrescription.setVisible(false);


            } else {
                System.out.println("Helllo 11 >> ");
                tfConsumerCName.setVisible(true);
                tfConsumerCAddress.setVisible(true);
                tfConsumerDoctorName.setVisible(true);
                tfConsumerDoctorAddress.setVisible(true);
//                tfConsumerPrescription.setVisible(true);
                tfCounterSaleDiscPer.setVisible(true);
                lblConsumerCName.setVisible(true);
                lblConsumerCAddress.setVisible(true);
                lblConsumerDoctorName.setVisible(true);
                lblConsumerDoctorAddress.setVisible(true);
                lblConsumerPrescription.setVisible(true);
                btnConsumerPrescription.setVisible(true);

            }
        });

//       addRowInCounterSale.setOnAction(this::addRow);
//        cmpTRowData();

        // Product Window Close
//        btnCounterSalePrdWindowClose.setOnAction(actionEvent -> {
//            hbapCounterSaleProductWindow.setVisible(false);
//        });
        // Batch Window Close
//        btnCounterSaleBatWinClose.setOnAction(actionEvent -> {
//            hbapCounterSaleBatchWindow.setVisible(false);
//        });


        // Particular Product PopUp Open Code
        tblvCounterSaleView.setEditable(true);
        tblvCounterSaleView.getSelectionModel().setCellSelectionEnabled(true);
        tblvCounterSaleView.getSelectionModel().selectFirst();
        tblvCounterSaleView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                tblvCounterSaleView.getSelectionModel().select(oldSelection);
            }
        });


        getCounterSale();

        //Convert toinvoice button logic
        btnCounterSaleToSaleInvoice.setOnAction(e -> {
            //GlobalController.getInstance().addTabStatic(SALES_INVOICE_CREATE_SLUG,false);
            getCounterSaleToSaleInvByIdData();
        });


        //get Countersale Id

        tblvCounterSaleList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                CounterSaleDTO selectedItem = (CounterSaleDTO) tblvCounterSaleList.getSelectionModel().getSelectedItem();
                System.out.println("CounterSaleDTO CounterSaleDTO-->" + selectedItem.getId());

                if (selectedItem != null) {
                    getCounterSaleByIdData();
                    counterId = selectedItem.getId();
                } else {
                    counterId = "";
                }

            }
        });

        //get Consumer by Id

        tblcConsumerSaleList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {

                CounterSaleDTO selectedItem = (CounterSaleDTO) tblcConsumerSaleList.getSelectionModel().getSelectedItem();
                System.out.println("Consumer c-->" + selectedItem.getId());

                if (selectedItem != null) {
                    consumerId = selectedItem.getId();
                    getConsumerSaleByIdData();
                } else {
                    consumerId = "";
                }

            }
        });


        //create counter sale API call

        btnCounterSaleSubmit.setOnAction(event -> {
            System.out.println("Save Button Clicked");
            createCounterSale();
        });

        //Add prescription pop up window open
        btnConsumerPrescription.setOnAction(event -> {
            System.out.println("Add Precsription");
            consumerPrescriptionPopUp();
        });
        //Consumer sale list API
        getConsumerSale();


        // Set cell factory for the "Serial Number" column
        tblcCounterSaleSrNo.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || getIndex() < 0) {
                        setText(null);
                    } else {
                        // Set the serial number as the index of the row + 1
                        setText(String.valueOf(getIndex() + 1));
                    }
                }
            };
        });


        //todo:Open Patient Modal
        //todo:On Clicking on the Ledger Name TextField and Select the Ledger from the Modal
        Platform.runLater(() -> tfConsumerCName.setOnMouseClicked(actionEvent -> {
            Stage stage = (Stage) spCounterSaleRootPane.getScene().getWindow();
            SingleInputDialogs.openPatientPopUp(stage, "Patient", input -> {
                System.out.println("i am  ");
                System.out.println("i am  " + input[0]);
                System.out.println("i am  " + input[1]);
                System.out.println("i am  " + input[2]);
                System.out.println("i am  " + input[3]);
                tfConsumerCName.setText(input[1].toString());
                patientId = String.valueOf(input[0]);

            });

        }));

        //todo:Open Doctor Modal
        //todo:On Clicking on the Ledger Name TextField and Select the Ledger from the Modal
        Platform.runLater(() -> tfConsumerDoctorName.setOnMouseClicked(actionEvent -> {
            Stage stage = (Stage) spCounterSaleRootPane.getScene().getWindow();
            SingleInputDialogs.openDoctorPopUp(stage, "Doctor", input -> {

                System.out.println("gf" + input[0]);
                tfConsumerDoctorName.setText(input[0].toString());
                doctorId = String.valueOf(input[1]);

                //todo:Callback to Get all the Details of the Selected ledger and Set it in the Fields

            });

        }));

        //todo:Open Product on click of add prescription product textfield Modal
        //todo:On Clicking on the Ledger Name TextField and Select the Ledger from the Modal
        Platform.runLater(() -> tfConsumerCName.setOnMouseClicked(actionEvent -> {
            Stage stage = (Stage) spCounterSaleRootPane.getScene().getWindow();
            SingleInputDialogs.openPatientPopUp(stage, "Patient", input -> {

                System.out.println("i am  " + input[0]);
                System.out.println("i am  " + input[1]);
                System.out.println("i am  " + input[2]);
                System.out.println("i am  " + input[3]);
                tfConsumerCName.setText(input[1].toString());
                patientId = String.valueOf(input[0]);

            });

        }));
        sceneInitilization();


        //todo: checkbox creation in select column
        tblcCounterSaleSelect.setCellFactory(column -> new CheckBoxTableCell<>());
        tblcCounterSaleSelect.setCellValueFactory(cellData -> {
            CounterSaleDTO cellValue = cellData.getValue();
            BooleanProperty property = cellValue.isRowSelectedProperty();


            // Add listener to handler change
            property.addListener((observable, oldValue, newValue) -> cellValue.setIsRowSelected(newValue));
            findOutSelectedRow();
            if (cellValue.isIsRowSelected()) {
                System.out.println("helllo ");
                counterId = cellValue.getId();
                handleSelection(tblvCounterSaleList.getItems());

            } else {
                counterId = "";
            }
            return property;
        });

        counterPaymentModeData();
        counterDataTypeFilterData();


        CommonValidationsUtils.restrictMobileNumber(tfCounterCreateMobile);
    }


    public void counterSaleListTableDesign() {
        tblcCounterSaleSelect.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.03));
        tblcCounterSaleListInvNo.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.08));
        tblcCounterSaleListInvDate.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.06));
        tblcCounterSaleListMobile.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.06));
        tblcCounterSaleListQty.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.05));
        tblcCounterSaleListBillAmt.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.06));
        tblcCounterSaleListPayMode.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.06));
        tblcCounterSaleListProduct.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.27));
        tblcCounterSaleListPackage.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.05));
        tblcCounterSaleListUnit.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.05));
        tblcCounterSaleListBatch.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.09));
        tblcCounterSaleListRate.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.06));
        tblcCounterSaleListDis.prefWidthProperty().bind(tblvCounterSaleList.widthProperty().multiply(0.05));
    }

    public void consumerSaleListTableDesign() {
        tblcConsumerSaleListInvNo.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.08));
        tblcConsumerSaleListInvDate.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.06));
        tblcConsumerSaleListClientName.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.15));
        tblcConsumerSaleListClientAddress.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.15));
        tblcConsumerSaleListMobile.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.07));
        tblcConsumerSaleListQty.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.06));
        tblcConsumerSaleListBillAmt.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.10));
        tblcConsumerSaleListPayMod.prefWidthProperty().bind(tblcConsumerSaleList.widthProperty().multiply(0.08));

    }

    ///Radio Button selected value get
    public void counterDataTypeFilterData() {
        rbCounterSaleInvoice.setSelected(true);
        toggleGroupDatType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tblvCounterSaleList.setItems(originalData);
//                System.out.println("original data >> "+ originalData);
                RadioButton selectedRadioButton = (RadioButton) newValue;
                toggleGroupDatType.selectToggle(selectedRadioButton);
                radioDataType = selectedRadioButton.getText().toLowerCase();
                HashMap filterLst = new HashMap();
                System.out.println(" new value >. " + ((RadioButton) newValue).getId());
                System.out.println("Selected payment mode: " + radioDataType + "newValue" + newValue);
                Boolean data = selectedRadioButton.isSelected();
                if (radioDataType.equalsIgnoreCase("products")) {
                    tblcCounterSaleSelect.setVisible(true);
                    tblcCounterSaleListInvNo.setVisible(true);
                    tblcCounterSaleListInvDate.setVisible(true);
                    tblcCounterSaleListMobile.setVisible(true);
                    tblcCounterSaleListQty.setVisible(true);
                    tblcCounterSaleListBillAmt.setVisible(true);
                    tblcCounterSaleListProduct.setVisible(true);
                    tblcCounterSaleListPackage.setVisible(true);
                    tblcCounterSaleListUnit.setVisible(true);
                    tblcCounterSaleListBatch.setVisible(true);
                    tblcCounterSaleListRate.setVisible(true);
                    tblcCounterSaleListDis.setVisible(true);
                    tblcCounterSaleListPayMode.setVisible(true);

                } else {
                    tblcCounterSaleSelect.setVisible(true);
                    tblcCounterSaleListInvNo.setVisible(true);
                    tblcCounterSaleListInvDate.setVisible(true);
                    tblcCounterSaleListMobile.setVisible(true);
                    tblcCounterSaleListQty.setVisible(true);
                    tblcCounterSaleListBillAmt.setVisible(true);
                    tblcCounterSaleListProduct.setVisible(false);
                    tblcCounterSaleListPackage.setVisible(false);
                    tblcCounterSaleListUnit.setVisible(false);
                    tblcCounterSaleListBatch.setVisible(false);
                    tblcCounterSaleListRate.setVisible(false);
                    tblcCounterSaleListDis.setVisible(false);
                    tblcCounterSaleListPayMode.setVisible(true);

                }

            }
        });
    }


    ///Radio Button selected value get
    public void counterPaymentModeData() {
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tblvCounterSaleList.setItems(originalData);
//                System.out.println("original data >> "+ originalData);
                RadioButton selectedRadioButton = (RadioButton) newValue;
                toggleGroup.selectToggle(selectedRadioButton);
                radioPaymentMode = selectedRadioButton.getText().toLowerCase();
                HashMap filterLst = new HashMap();
                System.out.println(" new value >. " + ((RadioButton) newValue).getId());
                System.out.println("Selected payment mode: " + radioPaymentMode);
                filterTable(radioPaymentMode, tblvCounterSaleList);
            }
        });
    }

    public void filterTable(String filterString, TableView<CounterSaleDTO> tableView) {
        System.out.println("hellllo Filtertable Called >> " + filterString);
        FilteredList<CounterSaleDTO> filteredData = new FilteredList<>(tableView.getItems(), p -> true);
        filteredData.setPredicate(person -> {
            if (filterString == null || filterString.isEmpty()) {

                return true;
            }
            String lowerCaseFilter = filterString.toLowerCase();
            if (person.getPaymentMode().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });

        System.out.println("filteredData" + filteredData);
        if (!filteredData.isEmpty()) {
            tblvCounterSaleList.setItems(filteredData);
        } else if (filterString.equalsIgnoreCase("all")) {
            tblvCounterSaleList.setItems(originalData);
        }
    }


    @FXML
    private void handleChooseFile() {

//        FileChooser fileChooser = new FileChooser();
//
//        // Set extension filters
//        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
//        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
//        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx, *.xls)", "*.xlsx", "*.xls");
//        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json");
//
//        // Add filters to fileChooser
//        fileChooser.getExtensionFilters().addAll(textFilter, pdfFilter, excelFilter, jsonFilter);
//        // Show open file dialog
//        Stage stage = (Stage) btnPurChallChooseFileButton.getScene().getWindow();
//        File selectedFile = fileChooser.showOpenDialog(stage);
//
//        // Handle selected file
//        if (selectedFile != null) {
//            tfPurChallChooseFileText.setText(selectedFile.getName());
//        }
    }

    private void handleSelection(ObservableList<CounterSaleDTO> items) {
        String errorMessage = "Error: Different Ledger selected!";
        String jsonFormat = " [%s] ";   //"selectedIds":

        StringJoiner selectedIds = new StringJoiner(",");
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        String lastLedgerName = null;

        for (CounterSaleDTO item : items) {
            if (item.isIsRowSelected()) {
//                if (lastLedgerName == null) {
//                    lastLedgerName = item.getSundry_debtors_name();
//                } else if (!lastLedgerName.equals(item.getSundry_debtors_name())) {
//                    // Ledger names are different
//                    System.out.println("error mes");
//                    showError(errorMessage);
//                    return;
//                }
                obj.addProperty("id", item.getId());
                selectedIds.add(String.valueOf(obj));
            }
        }
        array.add(selectedIds.toString());
        System.out.println("obj " + array);
        // Ledger names are the same, show JSON format
        String jsonResult = "";
        if (!selectedIds.toString().isEmpty()) {
            jsonResult = String.format(jsonFormat, selectedIds);
            System.out.println("jsonResult1>>>>>> " + selectedIds);
            System.out.println("jsonResult>>>>>> " + jsonResult);
        }
        CounterToSaleInvId = jsonResult;


    }

    private void showError(String errorMessage) {
        AlertUtility.CustomCallback callback = (number) -> {
            if (number == 0) {
                tblvCounterSaleList.requestFocus();
            }
        };
        Stage stage = (Stage) spCounterSaleRootPane.getScene().getWindow();
        AlertUtility.AlertError(stage, AlertUtility.alertTypeError, errorMessage, callback);

    }

    //consumer sale prescription
    public void consumerPrescriptionPopUp() {
        SingleInputDialogs.openAddPrescriptionPopUp(Communicator.stage, "Prescription", input -> {


        });
    }


    private void findOutSelectedRow() {
        Boolean canShowButton = false;
        ObservableList<CounterSaleDTO> list = tblvCounterSaleList.getItems();
        for (CounterSaleDTO object : list) {
            if (object.isIsRowSelected()) {
                canShowButton = true;
//                for(int i= 0;i<Selected_index)
                Selected_index = Integer.valueOf(object.getId());
//                if(object.getSundry_debtors_name().toString())
                System.out.println("Selected Index " + Selected_index);
            }
        }
        if (canShowButton) {
            btnCounterSaleToSaleInvoice.setVisible(true);
//            btnSalesQuotationListToOrder.setVisible(true);
//            btnSalesQuotationListToChallan.setVisible(true);
//            btnSalesQuotationListToInvoice.setVisible(true);
        } else {
            btnCounterSaleToSaleInvoice.setVisible(false);
//            btnSalesQuotationListToOrder.setVisible(false);
//            btnSalesQuotationListToChallan.setVisible(false);
//            btnSalesQuotationListToInvoice.setVisible(false);
        }

    }


    //67890-
//    private void openAdjustBillWindow() {
////        Stage stage = (Stage) .getScene().getWindow()
//
//        System.out.println("Adjust MEnt window");
//
//        SingleInputDialogs.openAdjustBillPopUp(Communicator.stage, "Submit", input -> {
//
//
//            String productName = (String) input[0];
//            String productId = (String) input[1];
//            String packaging = (String) input[2];
//            String mrp = (String) input[3];
//            String unit = (String) input[4];
//            String taxper = (String) input[5];
//            String salesrate = (String) input[6];
//            String purchaseRate = (String) input[7];
//            String is_batch = (String) input[8];
//open
////            .setParticulars(productName);
////            setProduct_id(productId);
////       PaysetPackages(packaging);
////            tblvCounterSaleView.getTableRow().getItem().setIs_batch(is_batch);
////            tblvCounterSaleView.getTableRow().getItem().setTax(taxper);
////            tblvCounterSaleView.getTableRow().getItem().setB_no("1223");
////
////            List<LevelAForPurInvoice> levelAForPurInvoiceList = com.opethic.genivis.controller.tranx_sales.ProductUnitsPackingCS.getAllProductUnitsPackingFlavour(productId);
////            int index = tblvCounterSaleView.getTableRow().getIndex();
////            ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
////
////            if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
////                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);
////                tblvCounterSaleView.getTableRow().getItem().setLevelA(null);
////                tblvCounterSaleView.getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());
////
////
////            } else {
////                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
////                tblvCounterSaleView.getTableRow().getItem().setLevelA(null);
////                tblvCounterSaleView.getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());
////
////            }
//        });
//    }

    // Scene Initialization
    public void sceneInitilization() {
        spCounterSaleRootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof Stage) {
                Communicator.stage = (Stage) newScene.getWindow();
                LocalDate current_date = LocalDate.now();
                Communicator.tranxDate = current_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        });
    }


    TableCellCallback<Object[]> callback = item -> {

        System.out.println("item 1: " + item[0]);//QTY
        System.out.println("item 2: " + item[1]);//FREEqty
        System.out.println("item 3: " + item[2]);//net Amt
        System.out.println("item 4: " + item[3]);//gross amt
        System.out.println("item 5: " + item[4]);//dis amt
        System.out.println("item 6: " + item[5]);//taxable amt
        System.out.println("item 7: " + item[6]);//total tax amt
        System.out.println("item 8: " + item[7]);//totalFinalSgst
        System.out.println("item 9: " + item[8]);//totalFinalCgst

        System.out.println("taxCalculation " + item[9]);
        lblCounterSaleGrossTotal.setText(String.valueOf(item[3]));
        lblCounterSaleDis.setText(String.valueOf(item[4]));
        lblCounterSaleBillAmt.setText(String.valueOf(item[2]));
        lblCounterSaleTotal.setText(String.valueOf(item[2]));
        lblCounterSaleTax.setText((String) item[6]);

//    tvGST_Table.getItems().clear();
        ObservableList<GstDTO> gstDTOObservableList = FXCollections.observableArrayList();
        ObservableList<?> item9List = (ObservableList<?>) item[9];
        for (Object obj : item9List) {
            if (obj instanceof GstDTO) {
                gstDTOObservableList.add((GstDTO) obj);
            }
        }

//    tvGST_Table.getItems().addAll(gstDTOObservableList);

        JsonArray jsonArray = new JsonArray();
        for (GstDTO gstDTO : gstDTOObservableList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("d_gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer())));
            jsonObject.addProperty("gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer()) / 2));
            jsonObject.addProperty("amt", gstDTO.getCgst());
            jsonArray.add(jsonObject);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("cgst", jsonArray);
        jsonObject.add("sgst", jsonArray);
//        purchase_invoice_map.put("taxCalculation", jsonObject.toString());

//        lbBillAmount.setText((String) item[2]); //totalNetAmt for bill amount
//        totalamt = (String) item[2];

//        total_purchase_discount_amt = (String) item[4];

//        System.out.println("ledgerStateCode =>" + ledgerStateCode + "GlobalTranx.getCompanyStateCode() =>" + GlobalTranx.getCompanyStateCode());


//    if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
//        taxFlag = true;
//    }

//    purchase_invoice_map.put("total_qty", (String) item[0]);
//    purchase_invoice_map.put("total_free_qty", (String) item[1]);
//
//    purchase_invoice_map.put("total_base_amt", (String) item[3]);
//    purchase_invoice_map.put("total_invoice_dis_amt", (String) item[4]);
//    purchase_invoice_map.put("taxable_amount", (String) item[5]);
//    purchase_invoice_map.put("bill_amount", (String) item[2]);
//    purchase_invoice_map.put("total_tax_amt", (String) item[6]);
//
//    purchase_invoice_map.put("totaligst", (String) item[6]);
//    purchase_invoice_map.put("totalsgst", (String) item[7]);
//    purchase_invoice_map.put("totalcgst", (String) item[8]);
//
//
//    purchase_invoice_map.put("taxFlag", String.valueOf(taxFlag));
//
//    total_taxable_amt = Double.parseDouble((String) item[5]);
//
//
//    lbGrossTotal.setText((String) item[3]);
//    lbDiscount.setText((String) item[4]);
//    lbTotal.setText((String) item[5]);
//    lbTax.setText((String) item[6]);

    };

    TableCellCallback<Boolean> consCallback = item -> {
        System.out.println("item consCallback >> : "+ item);
        if(item == true){
            switchConsumer.switchOnProperty().set(true);
            Platform.runLater(()->{
                tfConsumerCName.requestFocus();
            });
        }else{
            switchConsumer.switchOnProperty().set(false);
        }
    };



        public void tableInitiliazation() {

        tblvCounterSaleView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblvCounterSaleView.setEditable(true);
        tblvCounterSaleView.setFocusTraversable(false);


//        Label headerLabel = new Label("Sr\nNo.");
//        tblcCounterSaleSrNo.setGraphic(headerLabel);

        tblvCounterSaleView.getItems().addAll(new CounterSaleRowDTO("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));

        tblcCounterSaleSrNo.setCellValueFactory(cellData -> cellData.getValue().sr_noProperty());
        tblcCounterSaleSrNo.setStyle("-fx-alignment: CENTER;");

        tblcCounterSalePackage.setCellValueFactory(cellData -> cellData.getValue().packagesProperty());
        tblcCounterSalePackage.setStyle("-fx-alignment: CENTER;");

        tblcCounterSaleLevelB.setCellValueFactory(cellData -> cellData.getValue().levelBProperty());
        tblcCounterSaleLevelB.setCellFactory(column -> new ComboBoxTableCellForCSLevelB("tblcCounterSaleLevelB"));

        tblcCounterSaleLevelA.setCellValueFactory(cellData -> cellData.getValue().levelAProperty());
        tblcCounterSaleLevelA.setCellFactory(column -> new ComboBoxTableCellForCSLevelA("tblcCounterSaleLevelA"));

        tblcCounterSaleLevelC.setCellValueFactory(cellData -> cellData.getValue().levelCProperty());
        tblcCounterSaleLevelC.setCellFactory(column -> new ComboBoxTableCellForCSLevelC("tblcCounterSaleLevelC"));

        tblcCounterSaleUnit.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        tblcCounterSaleUnit.setCellFactory(column -> new ComboBoxTableCellForCSUnit("tblcCounterSaleUnit"));

        tblcCounterSaleParticular.setCellValueFactory(cellData -> cellData.getValue().particularsProperty());
        tblcCounterSaleParticular.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleParticular", callback, consCallback));


        tblcCounterSaleBatchNo.setCellValueFactory(cellData -> cellData.getValue().batch_or_serialProperty());
        tblcCounterSaleBatchNo.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleBatchNo", callback));


        tblcCounterSaleQty.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        tblcCounterSaleQty.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleQty", callback));

        tblcCounterSaleRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty());
        tblcCounterSaleRate.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleRate", callback));

        tblcCounterSaleDIscPer.setCellValueFactory(cellData -> cellData.getValue().dis_perProperty());
        tblcCounterSaleDIscPer.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleDIscPer", callback));

        tblcCounterSaleNetAmt.setCellValueFactory(cellData -> cellData.getValue().net_amountProperty());
        tblcCounterSaleNetAmt.setCellFactory(column -> new TextFieldTableCellForCounterSale("tblcCounterSaleNetAmt", callback));

        tblcCounterSaleAction.setCellValueFactory(cellData -> cellData.getValue().net_amountProperty());
        tblcCounterSaleAction.setCellFactory(column -> new ButtonTableCellForCounterSale());

        columnVisibility(tblcCounterSaleLevelA, Globals.getUserControlsWithSlug("is_level_a"));
        columnVisibility(tblcCounterSaleLevelB, Globals.getUserControlsWithSlug("is_level_b"));
        columnVisibility(tblcCounterSaleLevelC, Globals.getUserControlsWithSlug("is_level_c"));

        tblcCounterSaleLevelA.setText(Globals.getUserControlsNameWithSlug("is_level_a") + "");
        tblcCounterSaleLevelB.setText(Globals.getUserControlsNameWithSlug("is_level_B") + "");
        tblcCounterSaleLevelC.setText(Globals.getUserControlsNameWithSlug("is_level_C") + "");


    }

    private void columnVisibility(TableColumn<CounterSaleRowDTO, String> column, boolean visible) {
        if (visible) {
            //  column.setPrefWidth(USE_COMPUTED_SIZE);
            //column.setMinWidth(USE_PREF_SIZE);
            column.setMaxWidth(Double.MAX_VALUE);
        } else {
            column.setPrefWidth(0);
            column.setMinWidth(0);
            column.setMaxWidth(0);
        }
    }


    public void responsiveCmpt() {
        tblcCounterSaleSrNo.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.03));
        tblcCounterSaleParticular.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.35));
        tblcCounterSalePackage.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.04));
        tblcCounterSaleLevelA.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleLevelB.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleLevelC.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleUnit.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleBatchNo.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleQty.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.05));
        tblcCounterSaleRate.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleDIscPer.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleNetAmt.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));
        tblcCounterSaleAction.prefWidthProperty().bind(tblvCounterSaleView.widthProperty().multiply(0.06));

    }


    @FXML
    private void handleRadioButtonAction() {
        RadioButton selectedRadioButton = (RadioButton) rbPaymentMode.getSelectedToggle();
        if (selectedRadioButton != null) {
            selectedValue = selectedRadioButton.getText();
            System.out.println("Selected option: " + selectedValue);
            // Append selectedValue to your desired location
        }
    }

    public void fetchDataOfAllTransactionProducts() {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("search", "");
            body.put("barcode", "");
            String requestBody = Globals.mapToString(body);
            HttpResponse<String> response = APIClient.postJsonRequest(requestBody, "transaction_product_list_new");
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            ObservableList<TranxProductWindowDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseArray = jsonObject.get("list").getAsJsonArray();

                if (responseArray.size() > 0) {
                    tblvPurChallanPrdWindtableview.setVisible(true);
                    for (JsonElement element : responseArray) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String code = item.get("code").getAsString();
                        String product_name = item.get("product_name").getAsString();
                        String packing = item.get("packing").getAsString();
                        String barcode = "";
                        //String barcode = item.get("barcode").getAsString();
                        String brand = item.get("brand").getAsString();
                        String mrp = item.get("mrp") != null ? item.get("mrp").getAsString() : "0.00";
                        //  System.out.println("heeelo"+item.get("current_stock"));
                        String current_stock = item.get("current_stock").getAsString();
                        String unit = item.get("unit").getAsString();
                        String sales_rate = item.get("sales_rate") != null ? item.get("sales_rate").getAsString() : "";
                        String is_negative = item.get("is_negative").getAsString();
                        String batch_expiry = item.get("batch_expiry") != null ? item.get("batch_expiry").getAsString() : "";
                        String tax_per = item.get("tax_per") != null ? item.get("tax_per").getAsString() : "";
                        String is_batch = item.get("is_batch").getAsString();
                        String purchaserate = "";
                        //   String purchaserate= item.get("purchaserate").getAsString();

                        observableList.add(new TranxProductWindowDTO(id, code, product_name, packing, barcode, brand, mrp, current_stock, unit,
                                sales_rate, is_negative, batch_expiry, tax_per, is_batch, purchaserate)
                        );
                    }
                    tblcCounterSalePrdWindCode.setCellValueFactory(new PropertyValueFactory<>("code"));
                    tblcCounterSalePrdWindPrdName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
                    tblcCounterSalePrdWindPacking.setCellValueFactory(new PropertyValueFactory<>("packing"));
                    tblcCounterSalePrdWindBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
                    tblcCounterSalePrdWindBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
                    tblcCounterSalePrdWindMrp.setCellValueFactory(new PropertyValueFactory<>("mrp"));
                    tblcCounterSalePrdWindCurrStk.setCellValueFactory(new PropertyValueFactory<>("current_stock"));
                    tblcCounterSalePrdWindUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
                    tblcCounterSalePrdWindSaleRate.setCellValueFactory(new PropertyValueFactory<>("sales_rate"));

                    tblvPurChallanPrdWindtableview.setItems(observableList);

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


//    public void getUnitsData() {
//        Map<String, String> levelAMap = new HashMap<>();
//        Map<String, String> levelBMap = new HashMap<>();
//        Map<String, String> levelCMap = new HashMap<>();
//        Map<String, String> unitsMap = new HashMap<>();
//        unitList.clear();
//        Map<String, String> map = new HashMap<>();
//        map.put("product_id", productId);
//        String formData = Globals.mapToStringforFormData(map);
//        System.out.println("FormData: " + formData);
//        HttpResponse<String> response = APIClient.postFormDataRequest(formData, "get_all_product_units_packings_flavour");
//        responseBody = response.body();
//        UnitResDTO resDTO = new Gson().fromJson(responseBody, UnitResDTO.class);
//        if (resDTO.getResponseStatus() == 200) {
//            ResponseObject responseObject = resDTO.getResponseObject();
//            List<LstPackage> lstPackages = responseObject.getLstPackages();
//            for (LstPackage levelA : lstPackages) {
//                levelAMap.put(levelA.getLabel(), levelA.getValue());
//                List<LevelBOpt> levelBOpts = levelA.getLevelBOpts();
//                for (LevelBOpt levelB : levelBOpts) {
//                    levelBMap.put(levelB.getLabel(), levelB.getValue());
//                    List<LevelCOpt> levelCOpts = levelB.getLevelCOpts();
//                    for (LevelCOpt levelC : levelCOpts) {
//                        levelCMap.put(levelC.getLabel(), levelC.getValue());
//
//                        List<UnitListDTO> units = levelC.getUnitOpts();
//                        for (UnitListDTO unitListDTO : units) {
//                            unitsMap.put(unitListDTO.getLabel(), unitListDTO.getValue().toString());
//
//                            unitList.add(unitListDTO.getLabel());
//
//                        }
//                    }
//                }
//            }
//            tblcCounterSaleUnit.setCellFactory(column -> {
//                ComboBoxTableCell<CmpTRowDTO, String> cell = new ComboBoxTableCell<>();
//
//
//                cell.getItems().addAll(unitList);
//
//                cell.setConverter(new StringConverter<String>() {
//                    @Override
//                    public String toString(String s) {
//                        return s.toString();
//                    }
//
//                    @Override
//                    public String fromString(String s) {
//                        return null;
//                    }
//                });
//                return cell;
//            });
//
//        }
//
//    }


    public void totalCalculation(int rowIndex) {
        Double totalGrossAmt = 0.0;
        Double totalDisper = 0.0;
        Double grossAmt = 0.0;
        Double disPerAmt = 0.0;
        Double netAmt = 0.0;
        Double totalNetAmt = 0.0;


        for (CounterSaleRowDTO cmpTRowDTO : tblvCounterSaleView.getItems()) {
            grossAmt = Double.parseDouble(cmpTRowDTO.getGross_amount());
            disPerAmt = Double.valueOf(cmpTRowDTO.getDis_per_cal());
            netAmt = Double.parseDouble(cmpTRowDTO.getNet_amount());
            System.out.println("disPerAmt" + disPerAmt);


            totalGrossAmt += grossAmt;
            totalDisper += disPerAmt;
            totalNetAmt += netAmt;


        }
        lblCounterSaleGrossTotal.setText(String.valueOf(totalGrossAmt));
        lblCounterSaleDis.setText(String.valueOf(totalDisper));
        lblCounterSaleBillAmt.setText(String.valueOf(totalNetAmt));
        lblCounterSaleTotal.setText(String.valueOf(totalNetAmt));


        System.out.println("totalGrossAmt" + totalGrossAmt + "totalDisper" + totalDisper);
        System.out.println("totalNetAmt" + totalNetAmt);
    }

    public void createCounterSale() {
        System.out.println("counterId----" + counterId);
        rbPaymentModeCash.setToggleGroup(rbPaymentMode);
        rbPaymentModeMulti.setToggleGroup(rbPaymentMode);

        String mobileNo = tfCounterCreateMobile.getText();
        Boolean paymentMode = rbPaymentMode.getSelectedToggle().isSelected();

        System.out.println("paymentMode" + paymentMode + "mobileNo");
        DateConvertUtil.convertDateToLocalDate(new Date());

        Map<String, String> map = new HashMap<>();
        map.put("id", counterId);
        map.put("bill_dt", DateConvertUtil.convertDateToLocalDate(new Date()).toString());
        map.put("mobile_number", mobileNo);
        map.put("totalqty", "2");
        map.put("paymentMode", selectedValue);
        map.put("total_free_qty", "0");
        map.put("total_base_amt", lblCounterSaleGrossTotal.getText());
        map.put("total_row_gross_amt", lblCounterSaleGrossTotal.getText());
        map.put("bill_amount", lblCounterSaleBillAmt.getText());
        map.put("taxable_amount", lblCounterSaleTotal.getText());

        map.put("total_free_qty", "1");
        map.put("total_invoice_dis_amt", lblCounterSaleDis.getText());
        map.put("cashAmt", lblCounterSaleGrossTotal.getText());


        ArrayList<TranxPurRowDTO> rowData = new ArrayList<>();
        for (CounterSaleRowDTO cmpTRowDTO : tblvCounterSaleView.getItems()) {
            TranxPurRowDTO purParticularRow = new TranxPurRowDTO();
            purParticularRow.setProductId(cmpTRowDTO.getProduct_id());
            if (!cmpTRowDTO.getDetails_id().isEmpty() || cmpTRowDTO.getDetails_id()
                    != null) {
                purParticularRow.setDetailsId(cmpTRowDTO.getDetails_id());
            } else {
                purParticularRow.setDetailsId("0");
            }
            if (!cmpTRowDTO.getLevelA_id().isEmpty()) {
                purParticularRow.setLevelaId(cmpTRowDTO.getLevelA_id());
            } else {
                purParticularRow.setLevelaId("");
            }
            if (!cmpTRowDTO.getLevelB_id().isEmpty()) {
                purParticularRow.setLevelbId(cmpTRowDTO.getLevelB_id());
            } else {
                purParticularRow.setLevelbId("");
            }
            if (!cmpTRowDTO.getLevelC_id().isEmpty()) {
                purParticularRow.setLevelcId(cmpTRowDTO.getLevelC_id());
            } else {
                purParticularRow.setLevelcId("");
            }
            System.out.println("cmpTRowDTO.getUnit_id()" + cmpTRowDTO.getUnit_id());
            if (!cmpTRowDTO.getUnit().isEmpty()) {
                purParticularRow.setUnitId(cmpTRowDTO.getUnit_id());
            } else {
                purParticularRow.setUnitId("");
            }
            if (cmpTRowDTO.getUnit_conv() != null) {
                purParticularRow.setUnitConv(String.valueOf(cmpTRowDTO.getUnit_conv()));
            } else {
                purParticularRow.setUnitConv("0");
            }
            if (!cmpTRowDTO.getQuantity().isEmpty()) {
                purParticularRow.setQty(cmpTRowDTO.getQuantity());
            } else {
                purParticularRow.setQty("0");
            }
            if (!cmpTRowDTO.getFree().isEmpty()) {
                purParticularRow.setFreeQty(cmpTRowDTO.getFree());
            } else {
                purParticularRow.setFreeQty("0");
            }
            if (!cmpTRowDTO.getRate().isEmpty()) {
                purParticularRow.setRate(cmpTRowDTO.getRate());
            } else {
                purParticularRow.setRate("0");
            }
            if (cmpTRowDTO.getBase_amt() != null) {
                purParticularRow.setBaseAmt(String.valueOf(cmpTRowDTO.getBase_amt()));
            } else {
                purParticularRow.setBaseAmt("0");
            }
            if (!cmpTRowDTO.getDis_amt().isEmpty()) {
                purParticularRow.setDisAmt(cmpTRowDTO.getDis_amt());
            } else {
                purParticularRow.setDisAmt("0");
            }
            if (!cmpTRowDTO.getDis_per().isEmpty()) {
                purParticularRow.setDisPer(cmpTRowDTO.getDis_per());
            } else {
                purParticularRow.setDisPer("0");
            }
            if (!cmpTRowDTO.getDis_per().isEmpty()) {
                purParticularRow.setDisPer2("0.0");
            } else {
                purParticularRow.setDisPer2("0.0");
            }
            if (cmpTRowDTO.getDis_per_cal() != null) {
                purParticularRow.setDisPerCal(String.valueOf(cmpTRowDTO.getDis_per_cal()));
            } else {
                purParticularRow.setDisPerCal("0");
            }
            if (cmpTRowDTO.getDis_amt_cal() != null) {
                purParticularRow.setDisAmtCal(String.valueOf(cmpTRowDTO.getDis_amt_cal()));
            } else {
                purParticularRow.setDisAmtCal("0");
            }
            if (cmpTRowDTO.getRow_dis_amt() != null) {
                purParticularRow.setRowDisAmt(String.valueOf(cmpTRowDTO.getRow_dis_amt()));
            } else {
                purParticularRow.setRowDisAmt("0");
            }
            if (cmpTRowDTO.getGross_amount() != null) {
                purParticularRow.setGrossAmt(String.valueOf(cmpTRowDTO.getGross_amount()));
            } else {
                purParticularRow.setGrossAmt("0.0");
            }
            if (cmpTRowDTO.getAdd_chg_amt() != null) {
                purParticularRow.setAddChgAmt(String.valueOf(cmpTRowDTO.getAdd_chg_amt()));
            } else {
                purParticularRow.setAddChgAmt("0.0");
            }
            if (cmpTRowDTO.getGross_amount1() != null) {
                purParticularRow.setGrossAmt(String.valueOf(cmpTRowDTO.getGross_amount1()));
            } else {
                purParticularRow.setGrossAmt("0.0");
            }
            if (cmpTRowDTO.getInvoice_dis_amt() != null) {
                purParticularRow.setInvoiceDisAmt(String.valueOf(cmpTRowDTO.getInvoice_dis_amt()));
            } else {
                purParticularRow.setInvoiceDisAmt("0.0");
            }
            if (cmpTRowDTO.getTotal_amt() != null) {
                purParticularRow.setTotalAmt(String.valueOf(cmpTRowDTO.getTotal_amt()));
            } else {
                purParticularRow.setTotalAmt("0.0");
            }
            if (cmpTRowDTO.getGst() != null) {
                purParticularRow.setGst(String.valueOf(cmpTRowDTO.getGst()));
            } else {
                purParticularRow.setGst("");
            }
            if (cmpTRowDTO.getCgst() != null) {
                purParticularRow.setCgst(String.valueOf(cmpTRowDTO.getCgst()));
            } else {
                purParticularRow.setCgst("");
            }
            if (cmpTRowDTO.getIgst() != null) {
                purParticularRow.setIgst(String.valueOf(cmpTRowDTO.getIgst()));
            } else {
                purParticularRow.setIgst("");
            }
            if (cmpTRowDTO.getSgst() != null) {
                purParticularRow.setSgst(String.valueOf(cmpTRowDTO.getSgst()));
            } else {
                purParticularRow.setSgst("");
            }
            if (cmpTRowDTO.getTotal_igst() != null) {
                purParticularRow.setTotalIgst(String.valueOf(cmpTRowDTO.getTotal_igst()));
            } else {
                purParticularRow.setTotalIgst("");
            }
            if (cmpTRowDTO.getTotal_cgst() != null) {
                purParticularRow.setTotalCgst(String.valueOf(cmpTRowDTO.getTotal_cgst()));
            } else {
                purParticularRow.setTotalCgst("");
            }
            if (cmpTRowDTO.getTotal_sgst() != null) {
                purParticularRow.setTotalSgst(String.valueOf(cmpTRowDTO.getTotal_sgst()));
            } else {
                purParticularRow.setTotalSgst("");
            }
            if (cmpTRowDTO.getFinal_amount() != null) {
                purParticularRow.setFinalAmt(String.valueOf(cmpTRowDTO.getFinal_amount()));
            } else {
                purParticularRow.setSgst("");
            }
            if (cmpTRowDTO.getSgst() != null) {
                purParticularRow.setSgst(String.valueOf(cmpTRowDTO.getSgst()));
            } else {
                purParticularRow.setSgst("");
            }
            if (!cmpTRowDTO.getBatch_or_serial().isEmpty()) {
                purParticularRow.setbNo(cmpTRowDTO.getBatch_or_serial());
            } else {
                purParticularRow.setbNo("");
            }
//hjkl
            if (!cmpTRowDTO.getB_rate().isEmpty()) {
                purParticularRow.setbRate(cmpTRowDTO.getB_rate());
            } else {
                purParticularRow.setbRate("");
            }
            if (!cmpTRowDTO.getRate_a().isEmpty()) {
                purParticularRow.setRateA(cmpTRowDTO.getRate_a());
            } else {
                purParticularRow.setRateA("0");
            }

            if (!cmpTRowDTO.getRate_b().isEmpty()) {
                purParticularRow.setRateB(cmpTRowDTO.getRate_b());
            } else {
                purParticularRow.setRateB("0");
            }
            if (!cmpTRowDTO.getRate_c().isEmpty()) {
                purParticularRow.setRateC(cmpTRowDTO.getRate_c());
            } else {
                purParticularRow.setRateC("0");
            }
            if (!cmpTRowDTO.getCosting().isEmpty()) {
                purParticularRow.setCosting(cmpTRowDTO.getCosting());
            } else {
                purParticularRow.setCosting("0");
            }
            if (!cmpTRowDTO.getCosting_with_tax().isEmpty()) {
                purParticularRow.setCostingWithTax(cmpTRowDTO.getCosting_with_tax());
            } else {
                purParticularRow.setCostingWithTax("0");
            }
            if (!cmpTRowDTO.getMin_margin().isEmpty()) {
                purParticularRow.setMinMargin(cmpTRowDTO.getMin_margin());
            } else {
                purParticularRow.setMinMargin("0");
            }
            if (!cmpTRowDTO.getManufacturing_date().isEmpty()) {
                purParticularRow.setManufacturingDate(cmpTRowDTO.getManufacturing_date());
            } else {
                purParticularRow.setManufacturingDate("0");
            }
            if (!cmpTRowDTO.getB_purchase_rate().isEmpty()) {
                purParticularRow.setbPurchaseRate(cmpTRowDTO.getB_purchase_rate());
            } else {
                purParticularRow.setbPurchaseRate("0");
            }
            if (!cmpTRowDTO.getB_expiry().isEmpty()) {
                purParticularRow.setbExpiry(cmpTRowDTO.getB_expiry());
            } else {
                purParticularRow.setbExpiry("");
            }
            if (!cmpTRowDTO.getB_details_id().isEmpty()) {
                purParticularRow.setbDetailsId(cmpTRowDTO.getB_details_id());
            } else {
                purParticularRow.setbDetailsId("0");
            }
            if (!cmpTRowDTO.getIs_batch().isEmpty()) {
                purParticularRow.setBatch(cmpTRowDTO.getIs_batch());
            } else {
                purParticularRow.setBatch("0");
            }
            if (!cmpTRowDTO.getIs_serial().isEmpty()) {
                purParticularRow.setSerialNo(null);
            } else {
                purParticularRow.setSerialNo(null);
            }
            if (cmpTRowDTO.getReference_id().isEmpty()) {
                purParticularRow.setReferenceId(cmpTRowDTO.getReference_id());
            } else {
                purParticularRow.setReferenceId("");
            }
            if (cmpTRowDTO.getReference_type().isEmpty()) {
                purParticularRow.setReferenceType(cmpTRowDTO.getReference_type());
            } else {
                purParticularRow.setReferenceType("");
            }
            rowData.add(purParticularRow);
        }

        String mRowData = new Gson().toJson(rowData, new TypeToken<List<TranxPurRowDTO>>() {
        }.getType());
        System.out.println("mRowData=>" + mRowData);
        map.put("row", mRowData);
        String finalReq = Globals.mapToString(map);
        System.out.println("FinalReq=>" + finalReq);
        map.put("row", mRowData);


        HttpResponse<String> response;
        String formData = "";
        APIClient apiClient = null;
        Map<String, String> headers = new HashMap<>();
        if (isConsumer == true) {
            map.put("id", consumerId);
            System.out.println("Patient Id:" + patientId);
            map.put("debtors_id", patientId);
            map.put("client_name", tfConsumerCName.getText());
            map.put("patientName", tfConsumerCName.getText());
            map.put("client_address", tfConsumerCAddress.getText());
            map.put("doctorsId", doctorId);
            map.put("drAddress", tfConsumerDoctorAddress.getText());
            map.put("prescriptionList", "PAT#2");
            map.put("taxFlag", "false");
            formData = Globals.mapToStringforFormData(map);
            System.out.println("Form data:" + formData);

            if (consumerId.equalsIgnoreCase("")) {
                apiClient = new APIClient(EndPoints.CONSUMER_SALE_CREATE_ENDPOINT, formData, RequestType.FORM_DATA);

                // response = APIClient.postFormDataRequest(formData, EndPoints.CONSUMER_SALE_CREATE_ENDPOINT);
//                response = APIClient.postMultipartRequest(formData, null, EndPoints.CONSUMER_SALE_CREATE_ENDPOINT,
//                        headers);

            } else {
                apiClient = new APIClient(EndPoints.CONSUMER_SALE_UPDATE_ENDPOINT, formData, RequestType.FORM_DATA);

                // = APIClient.postFormDataRequest(formData, EndPoints.CONSUMER_SALE_UPDATE_ENDPOINT);
            }
        } else {
            formData = Globals.mapToStringforFormData(map);

            if (counterId.equalsIgnoreCase("")) {
                apiClient = new APIClient(EndPoints.COUNTER_SALE_CREATE_ENDPOINT, formData, RequestType.FORM_DATA);

                // response = APIClient.postFormDataRequest(formData, COUNTER_SALE_CREATE_ENDPOINT);

            } else {
                apiClient = new APIClient(EndPoints.COUNTER_SALE_UPDATE_ENDPOINT, formData, RequestType.FORM_DATA);

                // response = APIClient.postFormDataRequest(formData, COUNTER_SALE_UPDATE_ENDPOINT);
            }
        }
        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                System.out.println("Response=>" + responseBody);
                message = jsonObject.get("message").getAsString();

                if (jsonObject.get("responseStatus").getAsInt() == 200) {

//                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                    alert.setTitle("Success");
//                    alert.setHeaderText(message);
//                    // alert.setContentText("HSN has been created successfully.");
//                    alert.show();
//                    PauseTransition delay = new PauseTransition(Duration.seconds(1));
//                    delay.setOnFinished(event -> alert.close());
//                    delay.play();
                    AlertUtility.CustomCallback callback = (number) -> {

                        if (number == 1) {
//                                    GlobalController.getInstance().addTabStatic(FRANCHISE_LIST_SLUG, false);
                        }
                    };

                    Stage stage2 = (Stage) spCounterSaleRootPane.getScene().getWindow();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        AlertUtility.AlertSuccess(stage2, "Success", message, callback);
                    } else {
                        AlertUtility.AlertError(stage2, AlertUtility.alertTypeError, message, callback);
                    }
                    tblvCounterSaleView.getItems().clear();
                    tfCounterCreateMobile.setText("");
                    switchConsumer.switchOnProperty().set(false);
                    tfConsumerCName.setText("");
                    tfConsumerCAddress.setText("");
                    tfConsumerDoctorName.setText("");
                    tfConsumerDoctorAddress.setText("");
//                    tfConsumerPrescription.setText("");

                    tblvCounterSaleView.getItems().addAll(new CounterSaleRowDTO("", "0", "1", "", "", "", "", "", "",
                            "", "", "", "", "", "", "", "", ""));
//                    cmpTRowData();
                    getCounterSale();


                }
            }
        });

        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                logger.error("Network API cancelled in createCounterSale()" + workerStateEvent.getSource().getValue().toString());

            }
        });
        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                logger.error("Network API cancelled in createCounterSale()" + workerStateEvent.getSource().getValue().toString());

            }
        });
        apiClient.start();
        logger.debug("Create Patient Data End...");

//        JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);


    }

    //Counter Sale list
    public void getCounterSale() {

        try {

            rbCounterSalePMAll.setSelected(true);
            Map<String, String> map = new HashMap<>();
            map.put("payment_mode", radioPaymentMode);
            String formData = Globals.mapToStringforFormData(map);
            System.out.println("formData" + formData);

            HttpResponse<String> response = APIClient.postFormDataRequest(formData, COUNTER_SALE_GET_ENDPOINT);

            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println("counter sale jsonObject" + jsonObject);
            ObservableList<CounterSaleDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("row");
                System.out.println("row33333333333" + responseObject);

                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String details_id = item.get("details_id").getAsString();
                        String counterId = item.get("counterId").getAsString();
                        String countersrNo = item.get("countersrNo").getAsString();
                        String counterNo = item.get("counterNo").getAsString();
                        String invoiceDate = item.get("invoiceDate").getAsString();
                        String payment_mode = item.get("payment_mode").getAsString();
                        String mobile_number = item.get("mobile_number").getAsString();
//                        String productId = item.get("productId").getAsString();
//                        String patientCode = item.get("patientCode").getAsString();
                        String productName = item.get("productName").getAsString();
//                        String unitId = item.get("unitId").getAsString();
                        String unitName = item.get("unitName").getAsString();
                        String pack_name = item.get("pack_name").getAsString();
                        String qty = item.get("qty").getAsString();
//                        String free_qty=item.get("free_qty").getAsString();
                        String rate = item.get("rate").getAsString();
                        String dis_amt = item.get("dis_amt").getAsString();
                        String batch_no = item.get("purchase_rate").getAsString();
//                        String totalNetAmt=item.get("totalNetAmt").getAsString();
//                        String sales_rate=item.get("sales_rate").getAsString();

                        observableList.add(new CounterSaleDTO(id, countersrNo, invoiceDate, mobile_number, qty, dis_amt, payment_mode, productName,
                                        pack_name, unitName, batch_no, rate, dis_amt, "", ""
                                )
                        );
                    }
                    tblcCounterSaleListInvNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
                    tblcCounterSaleListInvDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
                    tblcCounterSaleListMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
                    tblcCounterSaleListQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
                    tblcCounterSaleListBillAmt.setCellValueFactory(new PropertyValueFactory<>("billAmt"));
                    tblcCounterSaleListPayMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
                    tblcCounterSaleListProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
                    tblcCounterSaleListPackage.setCellValueFactory(new PropertyValueFactory<>("packaging"));
                    tblcCounterSaleListUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
                    tblcCounterSaleListBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));
                    tblcCounterSaleListRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
                    tblcCounterSaleListDis.setCellValueFactory(new PropertyValueFactory<>("discount"));

                    tblvCounterSaleList.setItems(observableList);
                    originalData = tblvCounterSaleList.getItems();

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


    //Consumer Sale list

    public void getConsumerSale() {

        try {


            HttpResponse<String> response = APIClient.getRequest(EndPoints.CONSUMER_SALE_GET_ENDPOINT);

            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            System.out.println("counter sale jsonObject" + jsonObject);
            ObservableList<CounterSaleDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("response");
                System.out.println("row" + responseObject);

                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String saleSrNo = item.get("saleSrNo").getAsString();
                        String saleNo = item.get("saleNo").getAsString();
                        String saleDate = item.get("saleDate").getAsString();
                        String clientName = item.get("clientName").getAsString();
                        String clientAddress = item.get("clientAddress").getAsString();
                        String mobileNo = item.get("mobileNo").getAsString();
                        String totalQty = item.get("totalQty").getAsString();

                        String paymentMode = item.get("paymentMode").getAsString();

                        String totalBaseAmount = item.get("totalBaseAmount").getAsString();
                        String totalDiscount = item.get("totalDiscount").getAsString();
                        String totalsDiscountPer = item.get("totalsDiscountPer").getAsString();

                        String totalBill = item.get("totalBill").getAsString();

                        observableList.add(new CounterSaleDTO(id, saleNo, saleDate, mobileNo, totalQty, totalBill, paymentMode, "",
                                        "", "", "", totalBaseAmount, "", clientName, clientAddress
                                )
                        );
                    }

//                    tblcConsumerSaleListSelect.setCellValueFactory(new PropertyValueFactory<CounterSaleDTO, CheckBox>("select"));
                    // action.setCellFactory(column -> new CheckBoxTableCell<>());
//                    tblcConsumerSaleListSelect.setEditable(true);
                    tblcConsumerSaleListInvNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
                    tblcConsumerSaleListInvDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
                    tblcConsumerSaleListClientName.setCellValueFactory(new PropertyValueFactory<>("clientName"));
                    tblcConsumerSaleListClientAddress.setCellValueFactory(new PropertyValueFactory<>("clientAddress"));
                    tblcConsumerSaleListMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
                    tblcConsumerSaleListQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
                    tblcConsumerSaleListBillAmt.setCellValueFactory(new PropertyValueFactory<>("billAmt"));


                    tblcConsumerSaleList.setItems(observableList);

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


    public void getCounterSaleByIdData() {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("id", counterId);
            String finalReq = Globals.mapToStringforFormData(formData);

            HttpResponse<String> response = APIClient.postFormDataRequest(finalReq, EndPoints.COUNTER_SALE_GET_By_Id_ENDPOINT);
            CounterResDTO responseBody = new Gson().fromJson(response.body(), CounterResDTO.class);
            System.out.println("Get By Id" + responseBody.getResponseStatus());

            if (responseBody.getResponseStatus() == 200) {
                setCounterSaleData(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setCounterSaleData(CounterResDTO resDTO) {

        //  tblvCounterSaleView.getItems().clear();
        System.out.println("resDTO.getResponse().getRow()" + resDTO.getResponse().getRow());
        tfCounterCreateMobile.setText(resDTO.getResponse().getMobileNumber());
        tblvCounterSaleView.getItems().clear();
        for (Row mRow : resDTO.getResponse().getRow()) {
            CounterSaleRowDTO cmpTRowDTO = new CounterSaleRowDTO();
            cmpTRowDTO.setParticulars(mRow.getProductName());
            System.out.println("asdfgh >> : " + mRow.getQty());
            cmpTRowDTO.setQuantity(String.valueOf(mRow.getQty().intValue()));
            cmpTRowDTO.setRate(String.valueOf(mRow.getRate()));
            cmpTRowDTO.setBatch_or_serial(mRow.getBatchNo());
            cmpTRowDTO.setGross_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setTax("0");
            cmpTRowDTO.setNet_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setProduct_id(String.valueOf(mRow.getProductId()));
            cmpTRowDTO.setLevelA_id(String.valueOf(mRow.getLevelAId()));
            cmpTRowDTO.setLevelB_id(String.valueOf(mRow.getLevelBId()));
            cmpTRowDTO.setLevelC_id(String.valueOf(mRow.getLevelCId()));
            cmpTRowDTO.setUnit("1");
            cmpTRowDTO.setUnit_conv(String.valueOf(mRow.getUnitConv()));
            cmpTRowDTO.setFree(String.valueOf(mRow.getFreeQty()));
            // cmpTRowDTO.setBase_amt(String.valueOf(mRow.getBaseAmt()));
            cmpTRowDTO.setDis_amt(String.valueOf(mRow.getDisAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setB_no(mRow.getBatchNo());
            cmpTRowDTO.setB_rate(String.valueOf(mRow.getbRate()));
            cmpTRowDTO.setRate_a(String.valueOf(mRow.getMinRateA()));
            cmpTRowDTO.setRate_b(String.valueOf(mRow.getMinRateB()));
            cmpTRowDTO.setRate_c(String.valueOf(mRow.getMinRateC()));
            cmpTRowDTO.setCosting(String.valueOf(mRow.getCosting()));
            cmpTRowDTO.setCosting_with_tax(String.valueOf(mRow.getCostingWithTax()));
            cmpTRowDTO.setMin_margin(String.valueOf(mRow.getMinMargin()));
            cmpTRowDTO.setB_expiry(mRow.getbExpiry());
            cmpTRowDTO.setIs_batch(String.valueOf(mRow.getIsBatch()));
            cmpTRowDTO.setManufacturing_date(String.valueOf(mRow.getManufacturingDate()));
            cmpTRowDTO.setB_purchase_rate(String.valueOf(mRow.getPurchaseRate()));
            cmpTRowDTO.setB_details_id(String.valueOf(mRow.getbDetailsId()));
            //  cmpTRowDTO.set("");
            cmpTRowDTO.setReference_id("");
            cmpTRowDTO.setReference_type("");
            cmpTRowDTO.setDetails_id(String.valueOf(mRow.getDetailsId()));
            cmpTRowDTO.setBase_amt(String.valueOf(0.0));
            cmpTRowDTO.setRow_dis_amt(String.valueOf(0.0));
            cmpTRowDTO.setDis_per2(String.valueOf(mRow.getDisPer2()));
            cmpTRowDTO.setTotal_amt(String.valueOf(0.0));
            cmpTRowDTO.setIgst(String.valueOf(mRow.getIgst()));
            cmpTRowDTO.setCgst(String.valueOf(mRow.getCgst()));
            cmpTRowDTO.setSgst(String.valueOf(mRow.getSgst()));
            cmpTRowDTO.setTotal_igst(String.valueOf(mRow.getTotalIgst()));
            cmpTRowDTO.setTotal_sgst(String.valueOf(mRow.getTotalSgst()));
            cmpTRowDTO.setTotal_cgst(String.valueOf(mRow.getTotalCgst()));
            cmpTRowDTO.setFinal_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setIs_batch(String.valueOf(mRow.getIsBatch()));


            lblCounterSaleGrossTotal.setText(String.valueOf(mRow.getFinalAmt()));
            lblCounterSaleBillAmt.setText(String.valueOf(mRow.getFinalAmt()));

            tblvCounterSaleView.getItems().add(cmpTRowDTO);

//            rowCalculation(selectedRowIndex);


        }


    }

    public void getConsumerSaleByIdData() {
        try {
            Map<String, String> formData = new HashMap<>();
            formData.put("id", consumerId);
            String finalReq = Globals.mapToStringforFormData(formData);

            HttpResponse<String> response = APIClient.postFormDataRequest(finalReq, CONSUMER_SALE_GET_By_Id_ENDPOINT);
            ConsumerResDTO responseBody = new Gson().fromJson(response.body(), ConsumerResDTO.class);
            System.out.println("Get Consumer By Id" + responseBody.getResponse());
            if (responseBody.getResponseStatus() == 200) {
                setConsumerSaleData(responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void setConsumerSaleData(ConsumerResDTO resDTO) {
        System.out.println("resDTO >> " + resDTO.getResponse().getRow());

        tfCounterCreateMobile.setText(resDTO.getResponse().getMobileNumber());
        switchConsumer.switchOnProperty().set(true);
        tfConsumerCName.setText(resDTO.getResponse().getClientName());
        tfConsumerCAddress.setText(resDTO.getResponse().getClientAddress());
        tfConsumerDoctorName.setText(resDTO.getResponse().getDoctorId());
        tfConsumerDoctorAddress.setText(resDTO.getResponse().getDoctorAddress());
        tfConsumerPrescription.setText(resDTO.getResponse().getDoctorAddress());
        patientId = "PAT#1";
        doctorId = resDTO.getResponse().getDoctorId();
        System.out.println("patientId-------" + patientId);

        tblvCounterSaleView.getItems().clear();
        for (ConsumerRow mRow : resDTO.getResponse().getRow()) {
            CounterSaleRowDTO cmpTRowDTO = new CounterSaleRowDTO();
            cmpTRowDTO.setParticulars(mRow.getProductName());
            System.out.println("asdfgh >> : " + mRow.getQty());
            cmpTRowDTO.setQuantity(String.valueOf(mRow.getQty().intValue()));
            cmpTRowDTO.setRate(String.valueOf(mRow.getRate()));
            cmpTRowDTO.setBatch_or_serial(mRow.getBatchNo());
            cmpTRowDTO.setGross_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setTax("0");
            cmpTRowDTO.setNet_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setProduct_id(String.valueOf(mRow.getProductId()));
            cmpTRowDTO.setLevelA_id(String.valueOf(mRow.getLevelAId()));
            cmpTRowDTO.setLevelB_id(String.valueOf(mRow.getLevelBId()));
            cmpTRowDTO.setLevelC_id(String.valueOf(mRow.getLevelCId()));
            cmpTRowDTO.setUnit("1");
            cmpTRowDTO.setUnit_conv(String.valueOf(mRow.getUnitConv()));
            cmpTRowDTO.setFree(String.valueOf(mRow.getFreeQty()));
            // cmpTRowDTO.setBase_amt(String.valueOf(mRow.getBaseAmt()));
            cmpTRowDTO.setDis_amt(String.valueOf(mRow.getDisAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setB_no(mRow.getBatchNo());
            cmpTRowDTO.setB_rate(String.valueOf(mRow.getbRate()));
            cmpTRowDTO.setRate_a(String.valueOf(mRow.getMinRateA()));
            cmpTRowDTO.setRate_b(String.valueOf(mRow.getMinRateB()));
            cmpTRowDTO.setRate_c(String.valueOf(mRow.getMinRateC()));
            cmpTRowDTO.setCosting(String.valueOf(mRow.getCosting()));
            cmpTRowDTO.setCosting_with_tax(String.valueOf(mRow.getCostingWithTax()));
            cmpTRowDTO.setMin_margin(String.valueOf(mRow.getMinMargin()));
            cmpTRowDTO.setB_expiry(mRow.getbExpiry());
            cmpTRowDTO.setIs_batch(String.valueOf(mRow.getIsBatch()));
            cmpTRowDTO.setManufacturing_date(String.valueOf(mRow.getManufacturingDate()));
            cmpTRowDTO.setB_purchase_rate(String.valueOf(mRow.getPurchaseRate()));
            cmpTRowDTO.setB_details_id(String.valueOf(mRow.getbDetailsId()));
            //  cmpTRowDTO.setSerialNo("");
            cmpTRowDTO.setReference_id("");
            cmpTRowDTO.setReference_type("");
            cmpTRowDTO.setDetails_id(String.valueOf(mRow.getDetailsId()));
            cmpTRowDTO.setBase_amt(String.valueOf(0.0));
            cmpTRowDTO.setRow_dis_amt(String.valueOf(0.0));
            cmpTRowDTO.setDis_per2(String.valueOf(mRow.getDisPer2()));
            cmpTRowDTO.setTotal_amt(String.valueOf(0.0));
            cmpTRowDTO.setIgst(String.valueOf(mRow.getIgst()));
            cmpTRowDTO.setCgst(String.valueOf(mRow.getCgst()));
            cmpTRowDTO.setSgst(String.valueOf(mRow.getSgst()));
            cmpTRowDTO.setTotal_igst(String.valueOf(mRow.getTotalIgst()));
            cmpTRowDTO.setTotal_sgst(String.valueOf(mRow.getTotalSgst()));
            cmpTRowDTO.setTotal_cgst(String.valueOf(mRow.getTotalCgst()));
            cmpTRowDTO.setFinal_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setIs_batch(String.valueOf(mRow.getIsBatch()));
            cmpTRowDTO.setAdd_chg_amt(String.valueOf(mRow.getAddChgAmt()));
            cmpTRowDTO.setGross_amount1(String.valueOf(0.0));
            cmpTRowDTO.setDis_per_cal(String.valueOf(0.0));


            lblCounterSaleGrossTotal.setText(String.valueOf(mRow.getFinalAmt()));
            lblCounterSaleBillAmt.setText(String.valueOf(mRow.getFinalAmt()));
            tblvCounterSaleView.getItems().add(cmpTRowDTO);

//            rowCalculation(rowIndexBatchNo);

        }


    }

//Counter Sale to sale convert code:set data to row

    public void getCounterSaleToSaleInvByIdData() {
        try {

            System.out.println("SalesQuotId to update " + CounterToSaleInvId);
            Map<String, String> body = new HashMap<>();
            body.put("csno", CounterToSaleInvId);
            System.out.println("requestBody " + body);
            // loggerTranxSalesInvoiceCreate.debug("SalesQuotInvId 123 " + CounterToSaleInvId);
            String requestBody = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(requestBody, EndPoints.COUNTER_SALE_TO_SALE_INVOICE_GET_By_Id_ENDPOINT);
            //  loggerTranxSalesInvoiceCreate.debug("response data of SalesQuotToOrder " + response.body());
            CSToSInvDTO responseBody = new Gson().fromJson(response.body(), CSToSInvDTO.class);
            System.out.println("SalesQuotToOrderData " + responseBody.getResponseStatus());
            if (responseBody.getResponseStatus() == 200) {
                //todo: activating the ledger tab
                System.out.println("Api hit successfully" + responseBody.getRow());
                setCounterSalToSaleConvertData(responseBody);
                //todo: setting data in Ledger details block in sales quotation page

            } else {

                //loggerTranxSalesInvoiceCreate.error("Error in response of fetching Sales Quotation Data by id");
            }
        } catch (Exception e) {
            //loggerTranxSalesInvoiceCreate.error("Error in Fetching Sales Quotation Data by id is" + e.getMessage());
            e.printStackTrace();
        }

    }


    public void setCounterSalToSaleConvertData(CSToSInvDTO resDTO) {

        //  tblvCounterSaleView.getItems().clear();
        System.out.println("resDTO.getResponse().getRow()" + resDTO.getRow());
        // tfCounterCreateMobile.setText(resDTO.getMobileNumber());
        isConsumer = true;
        tblvCounterSaleView.getItems().clear();
        int count = 1;
        int index = 0;
        for (CSToSInvRowDTO mRow : resDTO.getRow()) {
            CounterSaleRowDTO cmpTRowDTO = new CounterSaleRowDTO();
            cmpTRowDTO.setParticulars(mRow.getProductName());
            System.out.println("asdfgh >> : " + mRow.getQty());
            cmpTRowDTO.setQuantity(String.valueOf(mRow.getQty().intValue()));
            cmpTRowDTO.setRate(String.valueOf(mRow.getRate()));
            cmpTRowDTO.setBatch_or_serial(mRow.getBatchNo());
            cmpTRowDTO.setGross_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setTax("0");
            cmpTRowDTO.setNet_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setProduct_id(String.valueOf(mRow.getProductId()));
            cmpTRowDTO.setLevelA_id(String.valueOf(mRow.getLevelAId()));
            cmpTRowDTO.setLevelB_id(String.valueOf(mRow.getLevelBId()));
            cmpTRowDTO.setLevelC_id(String.valueOf(mRow.getLevelCId()));
            cmpTRowDTO.setUnit(mRow.getUnitId().toString());
            cmpTRowDTO.setUnit_conv(String.valueOf(mRow.getUnitConv()));
            cmpTRowDTO.setFree(String.valueOf(mRow.getFreeQty()));
            // cmpTRowDTO.setBase_amt(String.valueOf(mRow.getBaseAmt()));
            cmpTRowDTO.setDis_amt(String.valueOf(mRow.getDisAmt()));
            cmpTRowDTO.setDis_per(String.valueOf(mRow.getDisPer()));
            cmpTRowDTO.setB_no(mRow.getBatchNo());
            cmpTRowDTO.setB_rate(String.valueOf(mRow.getbRate()));
            cmpTRowDTO.setRate_a(String.valueOf(mRow.getMinRateA()));
            cmpTRowDTO.setRate_b(String.valueOf(mRow.getMinRateB()));
            cmpTRowDTO.setRate_c(String.valueOf(mRow.getMinRateC()));
            cmpTRowDTO.setCosting(String.valueOf(mRow.getCosting()));
            cmpTRowDTO.setCosting_with_tax(String.valueOf(mRow.getCostingWithTax()));
            cmpTRowDTO.setMin_margin(String.valueOf(mRow.getMinMargin()));
            cmpTRowDTO.setB_expiry(mRow.getbExpiry());
            cmpTRowDTO.setIs_batch("");
            cmpTRowDTO.setManufacturing_date(String.valueOf(mRow.getManufacturingDate()));
            cmpTRowDTO.setB_purchase_rate(String.valueOf(mRow.getPurchaseRate()));
            cmpTRowDTO.setB_details_id(String.valueOf(mRow.getbDetailsId()));
            //  cmpTRowDTO.set("");
            cmpTRowDTO.setReference_id("");
            cmpTRowDTO.setReference_type("");
            cmpTRowDTO.setDetails_id(String.valueOf(mRow.getDetailsId()));
            cmpTRowDTO.setBase_amt(String.valueOf(0.0));
            cmpTRowDTO.setRow_dis_amt(String.valueOf(0.0));
            cmpTRowDTO.setDis_per2(String.valueOf(mRow.getDisPer2()));
            cmpTRowDTO.setTotal_amt(String.valueOf(0.0));
            cmpTRowDTO.setIgst(String.valueOf(mRow.getIgst()));
            cmpTRowDTO.setCgst(String.valueOf(mRow.getCgst()));
            cmpTRowDTO.setSgst(String.valueOf(mRow.getSgst()));
            cmpTRowDTO.setTotal_igst(String.valueOf(mRow.getTotalIgst()));
            cmpTRowDTO.setTotal_sgst(String.valueOf(mRow.getTotalSgst()));
            cmpTRowDTO.setTotal_cgst(String.valueOf(mRow.getTotalCgst()));
            cmpTRowDTO.setFinal_amount(String.valueOf(mRow.getFinalAmt()));
            cmpTRowDTO.setIs_batch("");

            System.out.println("product id : " + cmpTRowDTO.getProduct_id());
            List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPackingCS.getAllProductUnitsPackingFlavour(cmpTRowDTO.getProduct_id());
            System.out.println("levelAForPurInvoiceList >>  " + levelAForPurInvoiceList);
//            System.out.println("observableLevelAList >>  "+ observableLevelAList );

//
//            ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
//
//            System.out.println("levelAForPurInvoiceList >>  "+ levelAForPurInvoiceList );
//            System.out.println("observableLevelAList >>  "+ observableLevelAList );
//
//            System.out.println("Index >> : "+ index);
//            System.out.println("PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size() >> : "+ PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size());
//
//
//
//            if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
//                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);
//
//                tblvCounterSaleView.getItems().get(index).setLevelA(null);
//                tblvCounterSaleView.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());
//
//                for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
//                    if (tblvCounterSaleView.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
//                        tblvCounterSaleView.getItems().get(index).setLevelA(null);
//                        tblvCounterSaleView.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
//                    }
//                }
//
//            } else {
//                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
//
//                tblvCounterSaleView.getItems().get(index).setLevelA(null);
//                tblvCounterSaleView.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());
//
//                for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
//                    if (tblvCounterSaleView.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
//                        tblvCounterSaleView.getItems().get(index).setLevelA(null);
//                        tblvCounterSaleView.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
//                    }
//                }
//            }
//            count++;
//            index++;

            lblCounterSaleGrossTotal.setText(String.valueOf(mRow.getFinalAmt()));
            lblCounterSaleBillAmt.setText(String.valueOf(mRow.getFinalAmt()));

            tblvCounterSaleView.getItems().add(cmpTRowDTO);

//            rowCalculation(selectedRowIndex);


        }


    }


}


class TextFieldTableCellForCounterSale extends TableCell<CounterSaleRowDTO, String> {
    private TextField textField;
    private String columnName;

    TableCellCallback<Object[]> callback;
    TableCellCallback<Boolean> consCallback;


    public TextFieldTableCellForCounterSale(String columnName, TableCellCallback<Object[]> callback) {
        this.columnName = columnName;
        this.textField = new TextField();
        this.callback = callback;
        this.textField.setOnAction(event -> commitEdit(textField.getText()));
        // Commit when focus is lost
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(textField.getText());
            }
        });
        // Commit when Tab key is pressed
        this.textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                commitEdit(textField.getText());
            }
        });

        textfieldStyle();
        particularsColumn();
        disColumn();
        batchColumn();
        netAmountColumn();
        qtyColumn();
        rateColumn();
        discountPer();
        restrictToDecimalFields();

    }
    public TextFieldTableCellForCounterSale(String columnName, TableCellCallback<Object[]> callback, TableCellCallback<Boolean> consCallback) {
        this.columnName = columnName;
        this.textField = new TextField();
        this.callback = callback;
        this.consCallback = consCallback;
        this.textField.setOnAction(event -> commitEdit(textField.getText()));
        // Commit when focus is lost
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(textField.getText());
            }
        });
        // Commit when Tab key is pressed
        this.textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                commitEdit(textField.getText());
            }
        });

        textfieldStyle();
        particularsColumn();
        disColumn();
        batchColumn();
        netAmountColumn();
        qtyColumn();
        rateColumn();
        discountPer();
        restrictToDecimalFields();

    }

    public void restrictToDecimalFields() {
        if ("tblcCounterSaleDIscPer".equals(columnName)) {
            CommonValidationsUtils.restrictToDecimalNumbers(textField);
        }
        if ("tblcCounterSaleQty".equals(columnName)) {
            CommonValidationsUtils.restrictToDecimalNumbers(textField);
        }
        if ("tblcCounterSaleRate".equals(columnName)) {
            CommonValidationsUtils.restrictToDecimalNumbers(textField);
        }

    }

    private void batchColumn() {
        if ("tblcCounterSaleBatchNo".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    openBatchWindow();
                }
            });

            textField.setOnMouseClicked(event -> {
                openBatchWindow();
            });
        }
    }

    private void disColumn() {
        if ("tblcCounterSaleDIscPer".equals(columnName)) {
            textField.setEditable(false);
            textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                System.out.println("hello >>. : ");
                getTableView().getItems().addAll(new CounterSaleRowDTO("", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", ""));

            });


//            textField.setOnKeyPressed(event -> {
//                if (event.getCode() == KeyCode.ENTER) {
//                    System.out.println("hello >>. : ");
//                    getTableView().getItems().addAll(new CounterSaleRowDTO("", "", "", "", "", "", "", "", "",
//                            "", "", "", "", "", "", "", "", ""));
//
//                }
//            });


        }
    }


    private void particularsColumn() {
        if ("tblcCounterSaleParticular".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    openProduct();
                }
            });

            textField.setOnMouseClicked(event -> {
                openProduct();
            });
        }
    }

    private void netAmountColumn() {

        if ("tblcCounterSaleNetAmt".equals(columnName)) {
            textField.setEditable(true);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    System.out.println(getTableRow().getItem().getNet_amount());

                    int current_index = getTableRow().getIndex();

                    int serial_no = Integer.parseInt(getTableRow().getItem().getSr_no());

                    getTableView().getItems().addAll(new CounterSaleRowDTO("", String.valueOf(current_index + 1), String.valueOf(serial_no + 1), "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                }
            });
        }
    }


    private void qtyColumn() {
        if ("tblcCounterSaleQty".equals(columnName)) {
            textField.setEditable(true);

            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                    Integer index = getIndex();
                    counterSaleCalculation.rowCalculationForPurcInvoice(index, getTableView(), callback);
                }
            });
        }
    }

    private void rateColumn() {
        if ("tblcCounterSaleRate".equals(columnName)) {
            textField.setEditable(true);

            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                    Integer index = getIndex();
                    counterSaleCalculation.rowCalculationForPurcInvoice(index, getTableView(), callback);
                }
            });
        }
    }

    private void discountPer() {
        if ("tblcCounterSaleDIscPer".equals(columnName)) {
            textField.setEditable(true);

            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                    Integer index = getIndex();
                    counterSaleCalculation.rowCalculationForPurcInvoice(index, getTableView(), callback);
                }
            });
        }
    }


    private void openProduct() {
//        Stage stage = (Stage) .getScene().getWindow();

//        SingleInputDialogs.openProductPopUp(Communicator.stage, "Product", input -> {
//
//            String productName = (String) input[0];
//            String productId = (String) input[1];
//            String packaging = (String) input[2];
//            String mrp = (String) input[3];
//            String unit = (String) input[4];
//            String taxper = (String) input[5];
//            String salesrate = (String) input[6];
//            String purchaseRate = (String) input[7];
//            String is_batch = (String) input[8];
//
//            getTableRow().getItem().setParticulars(productName);
//            getTableRow().getItem().setProduct_id(productId);
//            getTableRow().getItem().setPackages(packaging);
//            getTableRow().getItem().setIs_batch(is_batch);
//            getTableRow().getItem().setTax(taxper);
//            // getTableRow().getItem().setB_no("1223");
//            getTableRow().getItem().setUnit(unit);
//
//            List<LevelAForPurInvoice> levelAForPurInvoiceList = com.opethic.genivis.controller.tranx_sales.ProductUnitsPackingCS.getAllProductUnitsPackingFlavour(productId);
//            int index = getTableRow().getIndex();
//            ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
//
//            System.out.println("levelAForPurInvoiceList >> :" + levelAForPurInvoiceList);
//            System.out.println("observableLevelAList >> :" + observableLevelAList);
//            System.out.println(" index >> :" + index);
//            System.out.println(" index PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size() >> :" + PurInvoiceCommunicator.levelAForPurInvoiceObservableList);
//
//            if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
//                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);
//                getTableRow().getItem().setLevelA(null);
//                getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());
//
//            } else {
//                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
//                getTableRow().getItem().setLevelA(null);
////                getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());
//
//            }
//        });
        CounterSaleRowDTO selectedRow = getTableView().getItems().get(getIndex());

        TranxCommonPopUps.openProductPopUp(Communicator.stage, Integer.valueOf(selectedRow.getProduct_id()), "Product", input -> {
//            Integer selectedIndex = tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().getSelectedIndex();
//            System.out.println("input);
            if (input != null) {
                getTableRow().getItem().setParticulars(input.getProductName());
                getTableRow().getItem().setProduct_id(input.getProductId().toString());
                getTableRow().getItem().setPackages(input.getPackageName());
                getTableRow().getItem().setIs_batch(input.getBatch().toString());
                getTableRow().getItem().setTax(input.getIgst().toString());
                System.out.println("input.getDrugType()  >. "+input.getDrugType());
                getTableRow().getItem().setDrugType(input.getDrugType());

                System.out.println("getTableRow().getItem().getDrugType()  >> "+getTableRow().getItem().getDrugType());
                if( getTableRow().getItem().getDrugType().equalsIgnoreCase("3")){
                    System.out.println("helllo >>> ; ");
                    if (consCallback != null) {
                        consCallback.call(true);
                    }
                }else{
                    if (consCallback != null) {
                        consCallback.call(false);
                    }
                }

//                getTableRow().getItem().setUnit(input.get());
                // getTableRow().getItem().setSelectedProduct(input);
                getTableRow().getItem().setRate(input.getSalesRate().toString());
//                getTableRow().getItem().
                List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPacking.getAllProductUnitsPackingFlavour(input.getProductId().toString());
                System.out.println("levelAForPurInvoiceList" + levelAForPurInvoiceList);
                //                int index = getTableRow().getIndex();
                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
                if (getIndex() >= 0 && getIndex() < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(getIndex(), observableLevelAList);
                    getTableRow().getItem().setLevelA(null);
                    getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                } else {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
                    getTableRow().getItem().setLevelA(null);
                    getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());
                }
                TableColumn<CounterSaleRowDTO, ?> colName = getTableView().getColumns().get(1);
                getTableView().edit(getIndex(), colName);
            }
        });
    }


    public void openBatchWindow() {
//        SingleInputDialogs.openBatchPopUp(Communicator.stage, "Batch", input -> {
//            System.out.println("i am in bosh");
//
//            System.out.println("i am madam ");
//            System.out.println("i am madam " + input[0]);
//            System.out.println("i am madam " + input[1]);
//            System.out.println("i am madam " + input[2]);
//            System.out.println("i am madam " + input[3]);
//            int selectedIndex = getIndex();
//            System.out.println("selectedIndex" + selectedIndex);
//
////            CounterSaleRowDTO newRow = tblvCounterSaleView.getItems().get(selectedIndex);
////            CounterSaleRowDTO newRow = getTableView().getItems().get(selectedIndex);
////            System.out.println("newRow.getB_no()"+input[0]);
//            String batchNo = (String) input[0];
//            String rate = (String) input[12];
//            String mrpRate = (String) input[4];
//            String b_details_id = (String) input[17];
//            System.out.println("batch no >> :" + batchNo);
//            getTableRow().getItem().setRate(mrpRate);
//            getTableRow().getItem().setBatch_or_serial(batchNo);
//            getTableRow().getItem().setB_rate(mrpRate);
//            getTableRow().getItem().setB_no(batchNo);
//            getTableRow().getItem().setB_details_id(b_details_id);
//            getTableRow().getItem().setRate_a(input[12].toString());
//            getTableRow().getItem().setRate_b(input[13].toString());
//            getTableRow().getItem().setRate_c(input[14].toString());
//            getTableRow().getItem().setCosting(input[15].toString());
//            getTableRow().getItem().setCosting_with_tax(input[16].toString());
//            getTableRow().getItem().setMin_margin("0");
//            getTableRow().getItem().setManufacturing_date(input[2].toString());
//            getTableRow().getItem().setB_purchase_rate(input[5].toString());
//            getTableRow().getItem().setB_expiry(input[3].toString());
//            getTableRow().getItem().setB_details_id(input[17].toString());
//            getTableRow().getItem().setIs_batch(String.valueOf(true));
//            //getTableRow().getItem().setBatch_or_serial("");
//            getTableRow().getItem().setReference_id("");
//            getTableRow().getItem().setReference_type("");
//
//            Platform.runLater(() -> {
//                getTableView().refresh();
//            });
//
//
//        });


        CounterSaleRowDTO selectedRow = getTableView().getItems().get(getIndex());
        if (Boolean.valueOf(selectedRow.getIs_batch()) == true) {
            TranxCommonPopUps.openBatchCSPopUpSales(Communicator.stage, selectedRow, Communicator.tranxDate, "Batch", input -> {
                if (input != null) {
                    //  selectedRow.setSelectedBatch(input);
                    Double mrp=input.getMrp();
                    Double id= (double) input.getId();
                    selectedRow.setB_details_id("" + input.getId());
                    selectedRow.setB_no(input.getBatchNo());
                    selectedRow.setBatch_or_serial(input.getBatchNo());
                    selectedRow.setRate(mrp.toString());
                    selectedRow.setB_details_id(id.toString());
            selectedRow.setRate_a(input.getUnit1FSRAI().toString());
            selectedRow.setRate_b(input.getUnit1FSRAI().toString());
            selectedRow.setRate_c(input.getUnit1FSRAI().toString());
            selectedRow.setCosting(input.getUnit1FSRAI().toString());
            selectedRow.setCosting_with_tax(input.getUnit1FSRAI().toString());
            selectedRow.setMin_margin("0");
            selectedRow.setManufacturing_date(input.getManufactureDate().toString());
           // selectedRow.setB_purchase_rate(input.getPurchaseRate());
            selectedRow.setB_expiry(input.getExpiryDate().toString());
            selectedRow.setB_details_id(input.getUnit1FSRAI().toString());
            selectedRow.setIs_batch(String.valueOf(true));
            //selectedRow.setBatch_or_serial("");
            selectedRow.setReference_id("");
            selectedRow.setReference_type("");

            Platform.runLater(() -> {
                getTableView().refresh();
            });
                }
                getTableView().getItems().set(getIndex(), selectedRow);
//                getBatchDetailsByBatchNoAndId(input.getBatchNo(), String.valueOf(input.getId()));
            });
        } else {
            selectedRow.setBatch_or_serial("#");
            selectedRow.setB_no("#");
            getTableView().getItems().set(getIndex(), selectedRow);
        }
        TableColumn<CounterSaleRowDTO, ?> colName = getTableView().getColumns().get(7);

        getTableView().edit(getIndex(), colName);
    }


    public void textfieldStyle() {
        if (columnName.equals("tblcCounterSaleParticular")) {
            textField.setPromptText("Particulars");
            textField.setEditable(false);
        } else {
            textField.setPromptText("0");
        }

        textField.setPrefHeight(38);
        textField.setMaxHeight(38);
        textField.setMinHeight(38);

        if (columnName.equals("tblcCounterSaleParticular")) {
            textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    textField.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 2; -fx-border-color: #00a0f5;");
                } else {
                    textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 2; -fx-border-color: #f6f6f9;");
                }
            });

            textField.setOnMouseEntered(e -> {
                textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");
            });

            textField.setOnMouseExited(e -> {
                if (!textField.isFocused()) {
                    textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
                }
            });
        } else {
            textField.setStyle("-fx-background-color: #f6f6f9; -fx-alignment: CENTER-RIGHT; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    textField.setStyle("-fx-background-color: #fff9c4; -fx-alignment: CENTER-RIGHT; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
                } else {
                    textField.setStyle("-fx-background-color: #f6f6f9; -fx-alignment: CENTER-RIGHT; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
                }
            });
            textField.setOnMouseEntered(e -> {
                textField.setStyle("-fx-background-color: #f6f6f9; -fx-alignment: CENTER-RIGHT; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");
            });

            textField.setOnMouseExited(e -> {
                if (!textField.isFocused()) {
                    textField.setStyle("-fx-background-color: #f6f6f9; -fx-alignment: CENTER-RIGHT; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
                }
            });
        }


//        if (columnName.equals("tblcCounterSaleNetAmt")) {
//            this.textField.setOnKeyPressed(event -> {
//                if (event.getCode() == KeyCode.ENTER) {
//                    commitEdit(textField.getText());
//                    getTableView().getItems().addAll(new CounterSaleRowDTO("", "", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
//                }
//            });
//        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(new VBox(textField));
        textField.requestFocus();

    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }


    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            VBox vbox = new VBox(textField);
            vbox.setAlignment(Pos.CENTER);
            textField.setText(item.toString());
            setGraphic(vbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        Object item = getTableRow().getItem();
        if (item != null && columnName.equals("tblcCounterSaleParticular")) {
            ((CounterSaleRowDTO) item).setParticulars(newValue.isEmpty() ? "" : newValue);
        } else if (columnName.equals("tblcCounterSaleBatchNo")) {
            System.out.println("newvalue >> " + newValue);
            (getTableRow().getItem()).setB_no(newValue.isEmpty() ? "" : newValue);
        } else if (columnName.equals("tblcCounterSaleQty")) {
            (getTableRow().getItem()).setQuantity(newValue.isEmpty() ? "0" : newValue);
        } else if (columnName.equals("tblcCounterSaleRate")) {
            (getTableRow().getItem()).setRate(newValue.isEmpty() ? "0.0" : newValue);
        } else if (columnName.equals("tblcCounterSaleDIscPer")) {
            (getTableRow().getItem()).setDis_per(newValue.isEmpty() ? "0.0" : newValue);
        } else if (columnName.equals("tblcCounterSaleNetAmt")) {
            (getTableRow().getItem()).setNet_amount(newValue.isEmpty() ? "0.0" : newValue);
        }

    }
}

class ComboBoxTableCellForCSLevelA extends TableCell<CounterSaleRowDTO, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForCSLevelA(String columnName) {

        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

        comboBox.setPrefHeight(38);
        comboBox.setMaxHeight(38);
        comboBox.setMinHeight(38);
        comboBox.setPrefWidth(139);
        comboBox.setMaxWidth(139);
        comboBox.setMinWidth(84);

        this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");

        this.comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                this.comboBox.show();
                this.comboBox.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
            } else {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
            }
        });

        this.comboBox.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered && !comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");

            } else if (!comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            }
        });

        if (comboList != null) {
            for (LevelAForPurInvoice commonDTO : comboList) {
                this.comboBox.getItems().add(commonDTO.getLabel());
            }
        }
        this.comboBox.setFocusTraversable(false);
        // AutoCompleteBox autoCompleteBox1 = new AutoCompleteBox<>(this.comboBox, -1);

        this.comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(String.valueOf(comboBox.getValue()));
            }
        });

        comBoAction();

    }

    public void comBoAction() {
        comboBox.setOnAction(e -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (PurInvoiceCommunicator.levelAForPurInvoiceObservableList != null && !PurInvoiceCommunicator.levelAForPurInvoiceObservableList.isEmpty()) {
                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(getTableRow().getIndex())) {
                        if (selectedItem.equals(levelAForPurInvoice.getLabel())) {

                            getTableRow().getItem().setLevelA_id(levelAForPurInvoice.getValue());

                            ObservableList<LevelBForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoice.getLevelBOpts());

                            int index = getTableRow().getIndex();

                            if (index >= 0 && index < PurInvoiceCommunicator.levelBForPurInvoiceObservableList.size()) {
                                PurInvoiceCommunicator.levelBForPurInvoiceObservableList.set(index, observableLevelAList);

                                getTableRow().getItem().setLevelB(null);
                                getTableRow().getItem().setLevelB(levelAForPurInvoice.getLevelBOpts().get(0).getLabel());

                                for (LevelBForPurInvoice levelBForPurInvoice : PurInvoiceCommunicator.levelBForPurInvoiceObservableList.get(index)) {
                                    if (levelBForPurInvoice.getValue().equals(getTableRow().getItem().getLevelB_id())) {
                                        getTableRow().getItem().setLevelB(null);
                                        getTableRow().getItem().setLevelB(levelBForPurInvoice.getLabel());
                                    }
                                }

                            } else {
                                PurInvoiceCommunicator.levelBForPurInvoiceObservableList.add(observableLevelAList);
                                getTableRow().getItem().setLevelB(null);
                                getTableRow().getItem().setLevelB(levelAForPurInvoice.getLevelBOpts().get(0).getLabel());

                                for (LevelBForPurInvoice levelBForPurInvoice : PurInvoiceCommunicator.levelBForPurInvoiceObservableList.get(index)) {
                                    if (levelBForPurInvoice.getValue().equals(getTableRow().getItem().getLevelB_id())) {
                                        getTableRow().getItem().setLevelB(null);
                                        getTableRow().getItem().setLevelB(levelBForPurInvoice.getLabel());
                                    }
                                }
                            }

                        }
                    }
                }
            }

        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(new HBox(comboBox));
        comboBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {

            comboBox.getItems().clear();

            if (item != null) {

                Platform.runLater(() -> {
                    int i = getTableRow().getIndex();
                    if (i >= 0 && i < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                        for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(i)) {
                            comboBox.getItems().add(levelAForPurInvoice.getLabel());
                        }
                        String itemToSet = item;
                        if (comboBox.getItems().contains(itemToSet)) {
                            comboBox.setValue(itemToSet);
                        }
                    }
                });
            }


            HBox hbox = new HBox();
            hbox.getChildren().addAll(comboBox);
            hbox.setSpacing(5);

            setGraphic(hbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        TableRow<CounterSaleRowDTO> row = getTableRow();
        if (row != null) {
            CounterSaleRowDTO item = row.getItem();
            if (item != null) {
                item.setLevelA(newValue);
            }
        }
    }

}

class ComboBoxTableCellForCSLevelB extends TableCell<CounterSaleRowDTO, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelBForPurInvoice> comboList = null;

    public ComboBoxTableCellForCSLevelB(String columnName) {
        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

        comboBox.setPrefHeight(38);
        comboBox.setMaxHeight(38);
        comboBox.setMinHeight(38);
        comboBox.setPrefWidth(139);
        comboBox.setMaxWidth(139);
        comboBox.setMinWidth(84);


        this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");

        this.comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                this.comboBox.show();
                this.comboBox.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
            } else {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
            }
        });

        this.comboBox.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered && !comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");

            } else if (!comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            }
        });

        if (comboList != null) {
            for (LevelBForPurInvoice commonDTO : comboList) {
                this.comboBox.getItems().add(commonDTO.getLabel());
            }
        }
        this.comboBox.setFocusTraversable(false);
        //  AutoCompleteBox autoCompleteBox1 = new AutoCompleteBox<>(this.comboBox, -1);

        this.comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(String.valueOf(comboBox.getValue()));
            }
        });

        comBoAction();
    }

    public void comBoAction() {
        comboBox.setOnAction(e -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (PurInvoiceCommunicator.levelBForPurInvoiceObservableList != null && !PurInvoiceCommunicator.levelBForPurInvoiceObservableList.isEmpty()) {
                    for (LevelBForPurInvoice levelBForPurInvoice : PurInvoiceCommunicator.levelBForPurInvoiceObservableList.get(getTableRow().getIndex())) {
                        if (selectedItem.equals(levelBForPurInvoice.getLabel())) {

                            getTableRow().getItem().setLevelB_id(levelBForPurInvoice.getValue());

                            ObservableList<LevelCForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelBForPurInvoice.getLevelCOpts());

                            int index = getTableRow().getIndex();


                            if (index >= 0 && index < PurInvoiceCommunicator.levelCForPurInvoiceObservableList.size()) {
                                PurInvoiceCommunicator.levelCForPurInvoiceObservableList.set(index, observableLevelAList);
                                getTableRow().getItem().setLevelC(null);

                                List<LevelCForPurInvoice> levelCOpts = levelBForPurInvoice.getLevelCOpts();
                                if (!levelCOpts.isEmpty()) {
                                    getTableRow().getItem().setLevelC(levelCOpts.get(0).getLabel());
                                }


                                for (LevelCForPurInvoice levelCForPurInvoice : PurInvoiceCommunicator.levelCForPurInvoiceObservableList.get(index)) {
                                    if (levelCForPurInvoice.getValue().equals(getTableRow().getItem().getLevelC_id())) {
                                        getTableRow().getItem().setLevelC(null);
                                        getTableRow().getItem().setLevelC(levelCForPurInvoice.getLabel());
                                    }
                                }

                            } else {
                                PurInvoiceCommunicator.levelCForPurInvoiceObservableList.add(observableLevelAList);
                                getTableRow().getItem().setLevelC(null);
                                getTableRow().getItem().setLevelC(levelBForPurInvoice.getLevelCOpts().get(0).getLabel());

                                for (LevelCForPurInvoice levelCForPurInvoice : PurInvoiceCommunicator.levelCForPurInvoiceObservableList.get(index)) {
                                    if (levelCForPurInvoice.getValue().equals(getTableRow().getItem().getLevelC_id())) {
                                        getTableRow().getItem().setLevelC(null);
                                        getTableRow().getItem().setLevelC(levelCForPurInvoice.getLabel());
                                    }
                                }
                            }

                        }
                    }
                }

            }

        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(new HBox(comboBox));
        comboBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {


            comboBox.getItems().clear();

            if (item != null) {

                Platform.runLater(() -> {
                    int i = getTableRow().getIndex();
                    if (i >= 0 && i < PurInvoiceCommunicator.levelBForPurInvoiceObservableList.size()) {
                        for (LevelBForPurInvoice levelBForPurInvoice : PurInvoiceCommunicator.levelBForPurInvoiceObservableList.get(i)) {
                            comboBox.getItems().add(levelBForPurInvoice.getLabel());
                        }
                        String itemToSet = item;
                        if (comboBox.getItems().contains(itemToSet)) {
                            comboBox.setValue(itemToSet);
                        }
                    }
                });
            }


            HBox hbox = new HBox();
            hbox.getChildren().addAll(comboBox);
            hbox.setSpacing(5);

            setGraphic(hbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        (getTableRow().getItem()).setLevelB(newValue);
    }

}

class ComboBoxTableCellForCSLevelC extends TableCell<CounterSaleRowDTO, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForCSLevelC(String columnName) {


        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

        comboBox.setPrefHeight(38);
        comboBox.setMaxHeight(38);
        comboBox.setMinHeight(38);

        comboBox.setPrefWidth(139);
        comboBox.setMaxWidth(139);
        comboBox.setMinWidth(84);

        this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");

        this.comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                this.comboBox.show();
                this.comboBox.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
            } else {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
            }
        });

        this.comboBox.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered && !comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");

            } else if (!comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            }
        });

        if (comboList != null) {
            for (LevelAForPurInvoice commonDTO : comboList) {
                this.comboBox.getItems().add(commonDTO.getLabel());
            }
        }
        this.comboBox.setFocusTraversable(false);
        //AutoCompleteBox autoCompleteBox1 = new AutoCompleteBox<>(this.comboBox, -1);

        this.comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(String.valueOf(comboBox.getValue()));
            }
        });

        comBoAction();

    }

    public void comBoAction() {
        comboBox.setOnAction(e -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (PurInvoiceCommunicator.levelCForPurInvoiceObservableList != null && !PurInvoiceCommunicator.levelCForPurInvoiceObservableList.isEmpty()) {

                    for (LevelCForPurInvoice levelCForPurInvoice : PurInvoiceCommunicator.levelCForPurInvoiceObservableList.get(getTableRow().getIndex())) {
                        if (selectedItem.equals(levelCForPurInvoice.getLabel())) {

                            getTableRow().getItem().setLevelC_id(levelCForPurInvoice.getValue());
                            ObservableList<UnitForPurInvoice> unitForPurInvoiceObservableList = FXCollections.observableArrayList(levelCForPurInvoice.getUnitOpts());

                            int index = getTableRow().getIndex();


                            if (index >= 0 && index < PurInvoiceCommunicator.unitForPurInvoiceList.size()) {
                                PurInvoiceCommunicator.unitForPurInvoiceList.set(index, unitForPurInvoiceObservableList);
                                getTableRow().getItem().setUnit(null);

                                List<UnitForPurInvoice> unitForPurInvoiceList = levelCForPurInvoice.getUnitOpts();
                                if (!unitForPurInvoiceList.isEmpty()) {
                                    getTableRow().getItem().setUnit(unitForPurInvoiceList.get(0).getLabel());
                                }

                                for (UnitForPurInvoice unitForPurInvoice : PurInvoiceCommunicator.unitForPurInvoiceList.get(index)) {
                                    if (unitForPurInvoice.getValue().equals(getTableRow().getItem().getUnit_id())) {
                                        getTableRow().getItem().setUnit(null);
                                        getTableRow().getItem().setUnit(unitForPurInvoice.getLabel());
                                    }
                                }

                            } else {
                                PurInvoiceCommunicator.unitForPurInvoiceList.add(unitForPurInvoiceObservableList);
                                getTableRow().getItem().setUnit(null);
                                getTableRow().getItem().setUnit(levelCForPurInvoice.getUnitOpts().get(0).getLabel());

                                for (UnitForPurInvoice unitForPurInvoice : PurInvoiceCommunicator.unitForPurInvoiceList.get(index)) {
                                    if (unitForPurInvoice.getValue().equals(getTableRow().getItem().getUnit_id())) {
                                        getTableRow().getItem().setUnit(null);
                                        getTableRow().getItem().setUnit(unitForPurInvoice.getLabel());
                                    }
                                }
                            }

                        }
                    }
                }
            }

        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(new HBox(comboBox));
        comboBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {

            comboBox.getItems().clear();

            if (item != null) {

                Platform.runLater(() -> {
                    int i = getTableRow().getIndex();
                    if (i >= 0 && i < PurInvoiceCommunicator.levelCForPurInvoiceObservableList.size()) {
                        for (LevelCForPurInvoice levelCForPurInvoice : PurInvoiceCommunicator.levelCForPurInvoiceObservableList.get(i)) {
                            comboBox.getItems().add(levelCForPurInvoice.getLabel());
                        }
                        String itemToSet = item;
                        if (comboBox.getItems().contains(itemToSet)) {
                            comboBox.setValue(itemToSet);
                        }
                    }
                });
            }


            HBox hbox = new HBox();
            hbox.getChildren().addAll(comboBox);
            hbox.setSpacing(5);

            setGraphic(hbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        (getTableRow().getItem()).setLevelC(newValue);
    }

}

class ComboBoxTableCellForCSUnit extends TableCell<CounterSaleRowDTO, String> {

    String columnName;
    private final ComboBox<String> comboBox;

    public ComboBoxTableCellForCSUnit(String columnName) {


        this.columnName = columnName;
        this.comboBox = new ComboBox<>();


        this.comboBox.setPromptText("Select");


        comboBox.setPrefHeight(38);
        comboBox.setMaxHeight(38);
        comboBox.setMinHeight(38);
        comboBox.setPrefWidth(179);
        comboBox.setMaxWidth(179);
        comboBox.setMinWidth(99);

        this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");

        this.comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                this.comboBox.show();
                this.comboBox.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
            } else {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
            }
        });

        this.comboBox.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered && !comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color:   #0099ff;");

            } else if (!comboBox.isFocused()) {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            }
        });

//        if (comboList != null) {
//            for (LevelAForPurInvoice commonDTO : comboList) {
//                this.comboBox.getItems().add(commonDTO.getLabel());
//            }
//        }
        this.comboBox.setFocusTraversable(false);


        this.comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(String.valueOf(comboBox.getValue()));
            }
        });

        comBoAction();


    }

    public void comBoAction() {
        comboBox.setOnAction(e -> {
            String selectedItem = comboBox.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (PurInvoiceCommunicator.unitForPurInvoiceList != null && !PurInvoiceCommunicator.unitForPurInvoiceList.isEmpty()) {

                    for (UnitForPurInvoice unitForPurInvoice : PurInvoiceCommunicator.unitForPurInvoiceList.get(getTableRow().getIndex())) {
                        if (selectedItem.equals(unitForPurInvoice.getLabel())) {
                            int index = getTableRow().getIndex();
                            getTableRow().getItem().setUnit_id(unitForPurInvoice.getValue());
                            getTableRow().getItem().setUnit_conv(String.valueOf(unitForPurInvoice.getUnitConversion()));

                        }
                    }
                }
            }

        });
    }


    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(new HBox(comboBox));
        comboBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {

            comboBox.getItems().clear();

            if (item != null) {

                Platform.runLater(() -> {
                    int i = getTableRow().getIndex();
                    if (i >= 0 && i < PurInvoiceCommunicator.unitForPurInvoiceList.size()) {
                        for (UnitForPurInvoice unitForPurInvoice : PurInvoiceCommunicator.unitForPurInvoiceList.get(i)) {
                            comboBox.getItems().add(unitForPurInvoice.getLabel());
                        }
                        String itemToSet = item;
                        if (comboBox.getItems().contains(itemToSet)) {
                            comboBox.setValue(itemToSet);
                        }
                    }
                });
            }


            HBox hbox = new HBox();
            hbox.getChildren().addAll(comboBox);
            hbox.setSpacing(5);

            setGraphic(hbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        (getTableRow().getItem()).setUnit(newValue);
    }

}

class ButtonTableCellForCounterSale extends TableCell<CounterSaleRowDTO, String> {
    private Button delete;

    public ButtonTableCellForCounterSale() {


        this.delete = createButtonWithImage();


        delete.setOnAction(actionEvent -> {
            CounterSaleRowDTO table = getTableView().getItems().get(getIndex());
            getTableView().getItems().remove(table);
            getTableView().refresh();
        });

    }


    private Button createButtonWithImage() {

        ImageView imageView = new ImageView(new Image(GenivisApplication.class.getResourceAsStream("/com/opethic/genivis/ui/assets/del.png")));
        imageView.setFitWidth(28);
        imageView.setFitHeight(28);
        Button button = new Button();
        button.setMaxHeight(38);
        button.setPrefHeight(38);
        button.setMinHeight(38);
        button.setPrefWidth(35);
        button.setMaxWidth(35);
        button.setMinWidth(35);
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: #f6f6f9;");
        button.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                button.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f86464");
            } else {
                button.setStyle("-fx-background-color: #f6f6f9;");
            }
        });

        button.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (isNowHovered && !button.isFocused()) {
                button.setStyle("-fx-background-color: #f6f6f9; -fx-border-width: 1; -fx-border-color: #f86464");
            } else if (!button.isFocused()) {
                button.setStyle("-fx-background-color: #f6f6f9;");
            }
        });

        return button;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
    }


    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            int rowIndex = getIndex();
            if (rowIndex == 0) {
                setGraphic(null);
            } else {
                HBox vbox = new HBox(delete);
                vbox.setAlignment(Pos.CENTER);
                setGraphic(vbox);
            }
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
    }

}

class ProductUnitsPackingCS {

    public static List<LevelAForPurInvoice> getAllProductUnitsPackingFlavour(String product_id) {


        Map<String, String> map = new HashMap<>();
        map.put("product_id", product_id);

        List<LevelAForPurInvoice> levelAForPurInvoicesList = new ArrayList<>();
        try {
            String formdata = mapToStringforFormData(map);

            HttpResponse<String> response = APIClient.postFormDataRequest(formdata, "get_all_product_units_packings_flavour");
            String responseBody = response.body();
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);

            int responseStatus = jsonObject.get("responseStatus").getAsInt();
            System.out.println("responseStatus: " + responseStatus);

            if (responseStatus == 200) {
                JsonObject resObj = jsonObject.get("responseObject").getAsJsonObject();
                JsonArray lstPackages = resObj.get("lst_packages").getAsJsonArray();

                System.out.println("resObjects >> " + resObj);
                System.out.println("lstPackages >> " + lstPackages);


//                LevelAForPurInvoice levelAForPurInvoice2 = new Gson().fromJson(lstPackages, LevelAForPurInvoice.class);
//
//                System.out.println("sout : " + levelAForPurInvoice2);


                for (JsonElement pack : lstPackages) {
                    System.out.println("pack =>" + pack);
                    try {
                        LevelAForPurInvoice levelAForPurInvoice = new Gson().fromJson(pack, LevelAForPurInvoice.class);
                        System.out.println("levelAForPurInvoice ->" + levelAForPurInvoice);
                        levelAForPurInvoicesList.add(levelAForPurInvoice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("getAllProductUnitsPackingFlavour levelAForPurInvoicesList >> " + levelAForPurInvoicesList);


            } else {
                System.out.println("responseObject is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return levelAForPurInvoicesList;
    }

}

class counterSaleCalculation {

    public static void rowCalculationForPurcInvoice(int rowIndex, TableView<CounterSaleRowDTO> tableView, TableCellCallback<Object[]> callback) {
        CounterSaleRowDTO purchaseInvoiceTable = tableView.getItems().get(rowIndex);
        String purchaseInvoiceTableData = purchaseInvoiceTable.getRate();
        System.out.println("In Row Calculation CMPTDATA=>" + purchaseInvoiceTableData);
        // Local parameter  to calculate gross amt and net amt
        String r_qty = "";  //declare to store row qty data
        Double r_rate = 0.0; //declare to store row rate data
        Double base_amt = 0.0; //declare to store base Amount data
        Double row_fin_grossAmt = 0.0; //declare to store row final gross amount
        Double r_dis_amt = 0.0; //declare to store row discount amount
        Double r_dis_per = 0.0; //declare to store row discount percent
        Double r_tax_per = 0.0; //declare to store row tax percent
        Double total_dis_amt = 0.0; //declare to store row total discount amount
        Double total_dis_per = 0.0; //declare to store row total discount percent
        Double row_dis_amt = 0.0; //declare to store total discount amt data
        Double total_grossAmt = 0.0;
        Double totalTax = 0.0; //to store total tax amt
        Double total_amt = 0.0; //to store total amt with discount minus
        Double net_amt = 0.0; // to store net  amount
        Double gross_amt = 0.0; //to store gross amount
        Double total_base_amt = 0.0; //store base amt
        Double dis_amt_cal = 0.0;
        Double total_taxable_amt = 0.0; //store total taxable amount
        Double taxable_amt = 0.0;//to store taxable amount
        Double igst = 0.0;
        Double cgst = 0.0;
        Double sgst = 0.0;
        Double totalIgst = 0.0;
        Double totalCgst = 0.0;
        Double totalSgst = 0.0;
        Double prevTaxPer = 0.0;
        Double finDisAmtCal = 0.0;
        Double disPer2 = 0.0;//to store discount per2

        //get the row level data by row index wise
        r_qty = purchaseInvoiceTable.getQuantity();
        r_rate = Double.parseDouble(purchaseInvoiceTable.getRate());
        if (!purchaseInvoiceTable.getDis_amt().isEmpty())
            r_dis_amt = Double.parseDouble(purchaseInvoiceTable.getDis_amt());
        if (!purchaseInvoiceTable.getDis_per().isEmpty())
            r_dis_per = Double.parseDouble(purchaseInvoiceTable.getDis_per());

        r_tax_per = Double.parseDouble(purchaseInvoiceTable.getTax());
        prevTaxPer = r_tax_per;


        //calculate base amount
        base_amt = Double.parseDouble(r_qty) * r_rate;
        gross_amt = base_amt;
        total_amt = base_amt;
        total_base_amt = base_amt;
        taxable_amt = base_amt;
        total_dis_amt = r_dis_amt;

        //calculate discount amt with gross amt

        if (r_dis_amt > 0) {
            gross_amt = gross_amt - total_dis_amt;
            total_amt = total_amt - total_dis_amt;
            total_base_amt = total_amt;
            dis_amt_cal = total_dis_amt;

            row_dis_amt = row_dis_amt + total_dis_amt;
            taxable_amt = total_amt;
        }

        //calculate discount percentage
        if (r_dis_per > 0) {
            total_dis_per = (r_dis_per / 100) * (gross_amt);

            gross_amt = gross_amt - total_dis_per;
            total_base_amt = total_base_amt - total_dis_per;
            total_amt = total_amt - total_dis_per;
            row_dis_amt = row_dis_amt + total_dis_per;
            taxable_amt = total_amt;

        }

        // tax GST and CGST and SGST Calculations

        if (r_tax_per >= 0) {
            totalTax = (r_tax_per / 100) * (total_amt);
            net_amt = total_amt + totalTax;

        }
        // set all calculated above data to CMPTROW DTO
        CounterSaleRowDTO selectedItem = tableView.getItems().get(rowIndex);
        selectedItem.setGross_amount(String.valueOf(base_amt));
        selectedItem.setNet_amount(String.valueOf(net_amt));
        selectedItem.setOrg_net_amt(String.valueOf(net_amt));
//        selectedItem.setOrg_taxable_amt(taxable_amt);
        selectedItem.setTaxable_amt(String.valueOf(taxable_amt));
        selectedItem.setTotal_taxable_amt(String.valueOf(total_taxable_amt));
        selectedItem.setIgst(String.valueOf(totalTax)); // for Tax Amount
        selectedItem.setSgst(String.valueOf(totalTax / 2)); // for Tax Amount
        selectedItem.setCgst(String.valueOf(totalTax / 2)); // for Tax  Amount
        selectedItem.setFinal_tax_amt(String.valueOf(totalTax));
        selectedItem.setFinal_dis_amt(String.valueOf(row_dis_amt));

        // Create API Payload Parameters Adding
        selectedItem.setBase_amt(String.valueOf(base_amt));
//        selectedItem.setGross_amt(String.valueOf(gross_amt);
        selectedItem.setDis_per(String.valueOf((r_dis_per)));
        selectedItem.setDis_per_cal(String.valueOf(total_dis_per));
        selectedItem.setDis_amt(String.valueOf((r_dis_amt)));
        selectedItem.setDis_amt_cal(String.valueOf(r_dis_amt));
        selectedItem.setRow_dis_amt(String.valueOf(row_dis_amt));
        selectedItem.setGross_amount1(String.valueOf(taxable_amt));
        // tax percentage store cmptrow
        selectedItem.setGst(String.valueOf(r_tax_per));
        selectedItem.setIgst(String.valueOf(r_tax_per));
        selectedItem.setSgst(String.valueOf(r_tax_per / 2));
        selectedItem.setCgst(String.valueOf(r_tax_per / 2));
        selectedItem.setTotal_igst(String.valueOf(totalTax));
        selectedItem.setTotal_sgst(String.valueOf(totalTax / 2));
        selectedItem.setTotal_cgst(String.valueOf(totalTax / 2));
        selectedItem.setFinal_amount(String.valueOf(net_amt));
        selectedItem.setDis_per2(String.valueOf(disPer2));

//        //Display data to table view
//        tblcPurChallCmpTRowGrossAmt.setCellValueFactory(new PropertyValueFactory<>("gross_amt"));
//        tblcPurChallCmpTRowNetAmt.setCellValueFactory(new PropertyValueFactory<>("net_amt"));
        calculateGst(tableView, callback);

    }

    public static void calculateGst(TableView<CounterSaleRowDTO> tableView, TableCellCallback<Object[]> callback) {


//        String addChgAmt = tfPurchaseChallanAddChgAmt.getText();
//        Double addChargAmount = 0.0;
//        if(!addChgAmt.isEmpty()){
//            addChargAmount = Double.parseDouble(addChgAmt);
//        }

        Map<Double, Double> cgstTotals = new HashMap<>(); //used to merge same cgst Percentage
        Map<Double, Double> sgstTotals = new HashMap<>();//used to merge same sgst Percentage
        // Initialize totals to zero for each tax percentage
        for (Double taxPercentage : new Double[]{0.0, 3.0, 5.0, 12.0, 18.0, 28.0}) {
            cgstTotals.put(taxPercentage, 0.0);
            sgstTotals.put(taxPercentage, 0.0);
        }
        Double taxPercentage = 0.0; // variable to store tax percantage
        Double totalQuantity = 0.0; // variable to store  total Quantity
        Double totalFreeQuantity = 0.0; // variable to store  totalFree Quantity
        Double freeQuantity = 0.0; // variable to store row wise free qty
        Double totalGrossAmt = 0.0, grossAmt = 0.0; // variable to store total Gross Amount and row wise gross amount
        Double totaltaxableAmt = 0.0, taxableAmt = 0.0; // variable to store total taxable Amount and row wise taxable amount
        Double totalDisAmt = 0.0, disAmt = 0.0; // variable to store total discount Amount and row wise discount amount
        Double totalTaxAmt = 0.0, taxAmt = 0.0;// variable to store total taxamount Amount and row wise taxamount amount
        Double totalNetAmt = 0.0, netAmount = 0.0; // variable to store total Net Amount and row wise Net amount


        Double totalFinalSgst = 0.00; // variable to store total SGST Amount
        Double totalFinalCgst = 0.00; // variable to store total CGST Amount
        Double totalFinalIgst = 0.00; // variable to store total IGST Amount
        Double rowDiscount = 0.0;

        // Calculate totals for each transaction
        for (CounterSaleRowDTO purchaseInvoiceTable : tableView.getItems()) {
            taxPercentage = Double.parseDouble(purchaseInvoiceTable.getTax());
            Double quantity = Double.parseDouble(purchaseInvoiceTable.getQuantity());
            if (purchaseInvoiceTable.getFree().isEmpty()) {
                freeQuantity = 0.0;
            } else {
                freeQuantity = Double.parseDouble(purchaseInvoiceTable.getFree());
            }

            // Total Calculations of each IGST, SGST, CGST
            totalQuantity += quantity;
            totalFreeQuantity += freeQuantity;
            Double igst = Double.parseDouble(purchaseInvoiceTable.getIgst().toString());
            Double cgst = Double.parseDouble(purchaseInvoiceTable.getCgst().toString());
            Double sgst = Double.parseDouble(purchaseInvoiceTable.getSgst().toString());
            totalFinalIgst += igst;
            totalFinalSgst += sgst;
            totalFinalCgst += cgst;

            cgstTotals.put(taxPercentage, cgstTotals.get(taxPercentage) + cgst); //0.0
            sgstTotals.put(taxPercentage, sgstTotals.get(taxPercentage) + sgst);

            //Total Calculation of gross amt ,taxable ,tax,discount
            netAmount = Double.parseDouble(purchaseInvoiceTable.getNet_amount());
            totalNetAmt += netAmount;
            grossAmt = Double.parseDouble(purchaseInvoiceTable.getGross_amount());
            totalGrossAmt += grossAmt;
            taxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt().toString());
            totaltaxableAmt += taxableAmt;
            disAmt = Double.parseDouble(purchaseInvoiceTable.getFinal_dis_amt().toString());
            totalDisAmt += disAmt;
            taxAmt = Double.parseDouble(purchaseInvoiceTable.getFinal_tax_amt().toString());
            totalTaxAmt += taxAmt;

        }

        ObservableList<GstDTO> observableList = FXCollections.observableArrayList();
        for (Double taxPer : new Double[]{3.0, 5.0, 12.0, 18.0, 28.0}) {
            double totalCGST = cgstTotals.get(taxPer);
            double totalSGST = sgstTotals.get(taxPer);
            double totalIGST = 0.0;

            if (totalCGST > 0) {
//                GstDTO gstDto :
                System.out.println("Tax Percentage: " + taxPer + "%");
                System.out.println("Total CGST: " + totalCGST);
                System.out.println("Total SGST: " + totalSGST);
                System.out.println();

                observableList.add(new GstDTO(String.format("%.2f", taxPer), String.format("%.2f", totalCGST), String.format("%.2f", totalSGST), String.valueOf(totalIGST)));

            }
//            tblcPurChallGST.setCellValueFactory(new PropertyValueFactory<>("taxPer"));
//            tblcPurChallCGST.setCellValueFactory(new PropertyValueFactory<>("cgst"));
//            tblcPurChallSGST.setCellValueFactory(new PropertyValueFactory<>("sgst"));
        }
//        tblvPurChallGST.setItems(observableList);
//        Platform.runLater(() -> {
//            tblvPurChallGST.refresh();
//
//        });

        if (callback != null) {

            Object[] object = new Object[10];

            object[0] = decimalFormat.format(totalQuantity);

            object[1] = String.valueOf(totalFreeQuantity);

            object[2] = String.format("%.2f", totalNetAmt);

            object[3] = String.format("%.2f", totalGrossAmt);

            object[4] = String.format("%.2f", totalDisAmt);

            //  lblPurChallTotalTaxableAmount.setText(String.format("%.2f", (totalGrossAmt - totalDisAmt + addChargAmount)));
            object[5] = String.format("%.2f", (totalGrossAmt - totalDisAmt + 0.0));

            object[6] = String.format("%.2f", totalTaxAmt);

            object[7] = String.format("%.2f", totalFinalSgst);

            object[8] = String.format("%.2f", totalFinalCgst);

            object[9] = observableList;


            if (callback != null) {
                callback.call(object);
            }
        }
    }


    public static void discountPropotionalCalculation(String disproportionalPer, String disproportionalAmt, String additionalCharges, TableView<CounterSaleRowDTO> tableView, TableCellCallback<Object[]> callback) {

        //get discount percentage and discount amount to textfield

        Double dis_proportional_per = 0.0;//store the textfield discount per String to double
        Double dis_proportional_amt = 0.0;//store the textfield discount amt String to double
        if (!disproportionalPer.isEmpty()) {// check if row level discount per is not empty
            //row level Discount Percentage
            dis_proportional_per = Double.parseDouble(disproportionalPer);
        } else {
            dis_proportional_per = 0.0;
        }
        if (!disproportionalAmt.isEmpty()) {// check if row level discount amt is not empty
            //row level Discount amount
            dis_proportional_amt = Double.parseDouble(disproportionalAmt);
        } else {
            dis_proportional_amt = 0.0;
        }
        System.out.println("dis_proportional_per discountPropotionalCalculation :: " + dis_proportional_per);
        Double finalNetAmt = 0.0;
        Double r_tax_per = 0.0;//store the Tax per data
        Double total_tax_amt = 0.0;//store total tax amount of each row level
        Double netAmt = 0.0;//Store net Amount after calculating  dis and tax
        Double rowTaxableAmt = 0.0; //store the row level calculated taxable amount
        Double totalTaxableAmt = 0.0;//store the total calculated taxable amount
        Double totalDisPropAmt = 0.0;//store the discount calculated proportional amt
        Double rowDisPropAmt = 0.0;//stotre the row level discount calculated amount
        Double totalTaxableAmtAdditional = 0.0;
        Double rowtotalTaxableAmtAdditional = 0.0;


        //calculate total taxable amount
        for (int i = 0; i < tableView.getItems().size(); i++) {
            System.out.println("Gross amt in Prop--->" + tableView.getItems().get(i).getGross_amount());
            rowCalculationForPurcInvoice(i, tableView, callback);//call row calculation function
            CounterSaleRowDTO purchaseInvoiceTable = tableView.getItems().get(i);
            totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt().toString());

        }
        System.out.println("totalTaxableAmt discountPropotionalCalculation :: " + totalTaxableAmt);

        Double rowDisc = 0.0;
        //get data of netamount, tax per,and row taxable amount from cmptrow
        for (CounterSaleRowDTO purchaseInvoiceTable : tableView.getItems()) {
            System.out.println("getFinal_dis_amt--->" + purchaseInvoiceTable.getFinal_dis_amt());
            rowDisc = Double.parseDouble(purchaseInvoiceTable.getFinal_dis_amt().toString());
            System.out.println("netAmt--->" + purchaseInvoiceTable.getNet_amount());
            netAmt = Double.parseDouble(purchaseInvoiceTable.getNet_amount());
            System.out.println("netAmt--->" + purchaseInvoiceTable.getTax());
            r_tax_per = Double.parseDouble(purchaseInvoiceTable.getTax());
            rowTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt().toString());
            System.out.println("rowTaxableAmt discountPropotionalCalculation :: " + rowTaxableAmt + "netAmt" + netAmt);

            //calculate discount proportional percentage and  the taxable amount
            if (dis_proportional_per > 0.0) {
                totalDisPropAmt = (dis_proportional_per / 100) * (totalTaxableAmt);
                rowDisPropAmt = (rowTaxableAmt * totalDisPropAmt) / totalTaxableAmt;
                System.out.println("row dis pro amt: " + rowDisPropAmt);
                rowTaxableAmt = rowTaxableAmt - rowDisPropAmt;
                rowDisc += rowDisPropAmt;
                totalTaxableAmtAdditional = rowTaxableAmt;
            } else {
                totalTaxableAmtAdditional = rowTaxableAmt;
            }

            System.out.println("rowDisc : " + rowDisc);


//
//            if (dis_proportional_amt > 0.0) {
//                totalDisPropAmt = dis_proportional_amt;
//                rowDisPropAmt = (rowTaxableAmt * totalDisPropAmt) / totalTaxableAmt;
//                rowDisc += rowDisPropAmt;
//                rowTaxableAmt = rowTaxableAmt - rowDisPropAmt;
//                totalTaxableAmtAdditional = rowTaxableAmt;
//            } else {
//                totalTaxableAmtAdditional = rowTaxableAmt;
//
//            }


            //calculate tax per and store the data to final net AMount
            if (additionalCharges.isEmpty()) {
                if (r_tax_per > 0) {
                    total_tax_amt = (r_tax_per / 100) * (rowTaxableAmt);
                    System.out.println("total_tax_amt" + total_tax_amt);
                    netAmt = rowTaxableAmt + total_tax_amt;
                    System.out.println("total netAmt  :" + netAmt + "" + rowTaxableAmt);
//
                }
            }

            //Set data to cmpTRow
            purchaseInvoiceTable.setNet_amount(String.format("%.2f", netAmt));
            purchaseInvoiceTable.setTotal_taxable_amt(String.valueOf(totalTaxableAmtAdditional));
            System.out.println("Total Tax Amt--->" + total_tax_amt);
            purchaseInvoiceTable.setIgst(String.valueOf(total_tax_amt));
            purchaseInvoiceTable.setCgst(String.valueOf(total_tax_amt / 2));
            purchaseInvoiceTable.setSgst(String.valueOf(total_tax_amt / 2));
            purchaseInvoiceTable.setFinal_tax_amt(String.valueOf(total_tax_amt));
            purchaseInvoiceTable.setFinal_dis_amt(String.valueOf(rowDisc));
            purchaseInvoiceTable.setInvoice_dis_amt(String.valueOf(rowDisPropAmt));

        }
        //Check additional charge is Empty or not if not empty call to the additional charges function
        if (!additionalCharges.isEmpty()) {
            //additionalChargesCalculation();
        } /*else {
            System.out.println("Additional Charges is empty");
            additionalChargesCalculation();
        }*/
        System.out.println("from discountPropotionalCalculation");
        calculateGst(tableView, callback);
    }


}


