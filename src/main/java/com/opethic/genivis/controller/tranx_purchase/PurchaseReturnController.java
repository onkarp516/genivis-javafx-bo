package com.opethic.genivis.controller.tranx_purchase;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.controller.dialogs.AdditionalCharges;
import com.opethic.genivis.controller.dialogs.BatchWindow;
import com.opethic.genivis.controller.dialogs.SingleInputDialogs;
import com.opethic.genivis.controller.master.ledger.common.LedgerMessageConsts;
import com.opethic.genivis.controller.tranx_sales.common.LineNumbersCellFactory;
import com.opethic.genivis.dto.*;
import com.opethic.genivis.dto.add_charges.CustomAddChargesDTO;
import com.opethic.genivis.dto.cmp_t_row.BatchWindowTableDTO;
import com.opethic.genivis.dto.pur_invoice.*;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.dto.reqres.product.TableCellCallback;
import com.opethic.genivis.dto.reqres.pur_tranx.*;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.opethic.genivis.utils.FxmFileConstants.PURCHASE_RETURN_LIST_SLUG;
import static com.opethic.genivis.utils.Globals.decimalFormat;
import static com.opethic.genivis.utils.Globals.mapToStringforFormData;
import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;
import static javafx.scene.control.PopupControl.USE_PREF_SIZE;


public class PurchaseReturnController implements Initializable {

    @FXML
    private TextField tfPurRtnLedgName, tfPurRtnInvNum, tfPurRtnsr;
    @FXML
    private Button btnPurChallChooseFileButton;

    @FXML
    private HBox topFirstHboxId, topSecondHboxId, pcBottomMain;
    @FXML
    private VBox totalCalBox, pcBottomFirstV, pcBottomSecondV, pcBottomThirdV, pcTotalMainDiv, pcTotalMainInnerDiv;
    @FXML
    private Region totalAmtRegion, gstRegion;

    @FXML
    private TableView<PurchaseInvoiceTable> tvPurInvCreateCMPTRow;
    @FXML
    private BorderPane bpPurRtnRootPane;
    @FXML
    public ComboBox<String> cmbRtnPaymentMode;
    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tblcPurInvCreateCmptSrNo, tblcPurInvCreateCmptParticular,
            tblcPurInvCreateCmptPackage, tblcPurInvCreateCmptUnit, tblcPurInvCreateCmptBatch,
            tblcPurInvCreateCmptQuantity, tblcPurInvCreateCmptRate, tblcPurInvCreateCmptGrossAmt,
            tblcPurInvCreateCmptDiscPerc, tblcPurInvCreateCmptDiscAmt, tblcPurInvCreateCmptTaxPerc, tblcPurInvCreateCmptNetAmt,
            tblcPurInvCreateCmptLevelA, tblcPurInvCreateCmptLevelB,
            tblcPurInvCreateCmptLevelC, tblcPurInvCreateCmptFreeQty;
    private ObservableList<CmpTRowDTO> itemList;
    private ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private ComboBox<GstDetailsDTO> cmbPurRtnLedgerGst;

    private int selectedRowIndex;
    private String SelectedLedgerId;
    private int actionPerformed = 0;

    private Integer purRtnEditId = -1;
    private String selectedPayMode = "Credit";

    @FXML
    private TableView<GstDTO> tblvPurRtnGST;
    @FXML
    private TableColumn<GstDTO, String> tblcPurRtnGST, tblcPurRtnCGST, tblcPurRtnSGST;
    @FXML
    private Label lblPurRtnRoundOff, lblPurRtnFreeQty, lblPurRtnTotalQty, lblPurRtnTotalBillAmount, lblPurRtnGrossTotal, lblPurRtnTotalTax,
            lblPurRtnTotalTaxableAmount, lblPurRtnTotalDiscount;
    @FXML
    private CheckBox chbPurRtnRoundOff;

    private String productId = "";
    private String responseBody;
    private static ObservableList<String> unitList = FXCollections.observableArrayList();
    private ComboBox<CommonDTO> cmbPackingText;

    private String prdBatchTaxPer;
    private String selectedRowPrdId;
    private String gstNumber;

    private Double costValue, costWithTaxValue, taxAmt, totalCgstTax = 0.0, totalSgstTax = 0.0;
    private static int rowIndexParticular;
    private static final Logger purRtnLogger = LoggerFactory.getLogger(PurchaseReturnController.class);
    private List<TranxPurRowDTO> tranxPurRowDTOS = new ArrayList<>();

    @FXML
    private Button btnPurRtnFinalSubmit, btnPurRtnCancel, btnPurRtnFinalModify, btnPurchaseRtnAddCharges;
    DecimalFormat decimalFormat = new DecimalFormat("0.#");

    @FXML
    private TabPane tranxPurRtnTabPane;
    @FXML
    private Tab tabPurRtnLedger, tabPurRtnProduct, tabPurRtnBatch;

    @FXML
    private TextField tfPurInvCreateTranxDate, tfPurRtnInvDate, tfPurRtnNarrtion;
    @FXML
    private ComboBox<CommonDTO> cmbPurRtnPurAC;
    private static ObservableList<TranxProductWindowDTO> observableListProduct = FXCollections.observableArrayList();

    @FXML
    private Text txtPurRtnGstNo, txtPurRtnArea, txtPurRtnBank, txtPurRtnContactPerson, txtPurRtnTransportName, txtPurRtnCreditDays, txtPurRtnFssai,
            txtPurRtnLicenseNo, txtPurRtnRoute, txtPurRtnProductBrand, txtPurRtnProductGroup, txtPurRtnProductSubGroup, txtPurRtnProductCategory, txtPurRtnProductHsn,
            txtPurRtnProductTaxType, txtPurRtnProductTaxPer, txtPurRtnProductMarginPer, txtPurRtnProductCost, txtPurRtnProductShelfId, txtPurRtnProductMinStock, txtPurRtnProductMaxStock,
            txtPurRtnBatchBatch, txtPurRtnBatchMRP, txtPurRtnBatchFSRMH, txtPurRtnBatchSaleRate, txtPurRtnBatchHMfgDt, txtPurRtnBatchPurRate, txtPurRtnBatchFSRAI,
            txtPurRtnBatchExpDt, txtPurRtnBatchDisPer, txtPurRtnBatchCSRMH, txtPurRtnBatchAvlStk, txtPurRtnBatchDisAmt, txtPurRtnBatchCSRAI;

    Long stateCode = 0L, CompanyStateCode = 0L;

    // Array to hold focusable nodes
    private Node[] focusableNodes;

    ArrayList<Double> netAmountList;

    private JsonObject jsonObject1 = null;
    private JsonObject jsonObject = null;

    @FXML
    private TableView<SalesOrderSupplierDetailsDTO> tvPurRtnSupplierDetails;
    @FXML
    private TableColumn<SalesOrderSupplierDetailsDTO, String> tcPCRtnPrdHistSupplierName, tcPCRtnPrdHistInvNo,
            tcPCRtnPrdHistInvDate, tcPCRtnPrdHistBatch, tcPCRtnPrdHistMRP, tcPCRtnPrdHistQty,
            tcPCRtnPrdHistRate, tcPCRtnPrdHistCost, tcPCRtnPrdHistDisPer, tcPCRtnPrdHistDisAmt;
    String message = "";

    @FXML
    private Label tranxDateLabel;


    private String ledgerName;
    private String ledgerId;
    private String invoiceId = "";


    private String src = "create";

    String purchaseAcID = "";

    String supplierGSTIN_ID = "";
    String gst_no = "";
    private String totalamt = "";
    private String total_purchase_discount_amt = "";
    private Boolean taxFlag = false;

    private String ledgerStateCode = "";

    private Double total_taxable_amt = 0.0;
    Map<String, String> purchase_return_map = new HashMap<>();

    private ObservableList<GstDetailsDTO> supplierGSTINList = FXCollections.observableArrayList();

    Map<String, Object> edit_response_map = new HashMap<>();

    Map<String, String> invoice_data_map = new HashMap<>();

    List<TranxPurRowResEditDTO> rowListDTOS = new ArrayList<>();
    private JsonArray deletedRows = new JsonArray();

    @FXML
    private HBox hbPurRtnGstTotal;
    @FXML
    private TextField tfPurRtnDisPer, tfPurRtnDisAmt, tfPurchaseRtnAddChgAmt, tfPurChallChooseFileText;
    @FXML
    private Label lblPurRtnTotalSGST, lblPurRtnTotalCGST;

    List<CustomAddChargesDTO> listEdit = new ArrayList<>();
    private PurchaseOrderDTO purchaseOrderDTO;
    List<CustomAddChargesDTO> addChargesDTOList = new ArrayList<>();
    private final List<AdditionalCharge> addChargesResLst = new ArrayList<>();

    /*    private HashMap <String,Object> prdRedDTO = null;*/

    Map<String, String> redirection_map = new HashMap<>();

    private Integer rediCurrIndex = -1;
    private Boolean isProductRed = false;
    private Boolean isLedgerRed = false;
    private Stage stage = null;
    private ObservableList<GstDetailsDTO> gstDetails = FXCollections.observableArrayList();
    private PurchaseInvoiceTable newRow = null;
    private List<PurchaseInvoiceTable> newRowList = new ArrayList<>();
    private ObservableList<String> payment_mode_list = FXCollections.observableArrayList("Credit", "Adjust");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Css Files Redirected  as Per Screen resolution
        resposiveScreenCss();
        // Edit Scenario Getting the Id
        purRtnEditId = PurchaseReturnListController.getPurRtnInvoiceId();
        PurchaseReturnListController.purchaseReturnid = -1;
        if (purRtnEditId > 0) {
            getPurRtnById(purRtnEditId);
        }

        tvPurRtnSupplierDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // GST Table Design and Data Set
        gstTableDesign();

        // autofocus on TranxDate
        Platform.runLater(() -> tfPurRtnLedgName.requestFocus());

        //Ledger Name Validations
        tfLedgerNameValidation();

        //Return Number Duplicate Check
        tfReturnNoValidation();

        // Enter Functionality Method
        initialEnterMethod();
        PurInvoiceCommunicator.levelAForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.levelBForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.levelCForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.unitForPurInvoiceList.clear();
        getPaymentModes();
        // To Redirect the Pur Return List page
//        redirectToListScreen();

        sceneInitilization();

        showDropDownOnKeyPress(cmbPurRtnLedgerGst);



        cmbRtnPaymentMode.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvPurInvCreateCMPTRow.edit(0, tvPurInvCreateCMPTRow.getColumns().get(1));
            }
        });
        cmbRtnPaymentMode.getSelectionModel().select("Credit");
        cmbRtnPaymentMode.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    cmbRtnPaymentMode.show();
                }
            }
        });
        // Table Initializtion for Create & Edit
        tableInitiliazation();


        btnPurchaseRtnAddCharges.setOnAction(e -> {
            onClickAdditionalCharges();
        });

        // Date Format In TextField Code and Date set validation for Edit and Create
        Platform.runLater(() -> {
            // dateSetValidation();
        });


        // Auto Set of Purchase Serial Number
        if (purRtnEditId > 0) {
            tfPurRtnsr.setText(invoice_data_map.get("purchase_sr_no"));
        } else {
            getPurRtnSrNumber();
        }

        // By Ledger Id Setting the Ledger Name And Gst Number
        if (purRtnEditId > 0) {
            System.out.println("S Id:" + invoice_data_map.get("supplierId"));
            tfPurRtnLedgName.setText(ledgersById(Long.valueOf(invoice_data_map.get("supplierId"))));
            if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
                cmbPurRtnLedgerGst.setItems(supplierGSTINList);
                cmbPurRtnLedgerGst.setValue(supplierGSTINList.get(0));
            }
        }
        if (purRtnEditId > 0) {
            String paymentMode = edit_response_map.get("paymentMode").toString();
            if (paymentMode.equalsIgnoreCase("credit")) {
                cmbRtnPaymentMode.getSelectionModel().select("Credit");
            }
            if (paymentMode.equalsIgnoreCase("adjust")) {
                cmbRtnPaymentMode.getSelectionModel().select("Adjust");
            }

        }
        // As Per Discount Fields Calling the ProportionalDiscount Calculation method in Edit Scenario
        discPropDataSetInEdit();

        //Purchase Accounts Set In ComboBox
        getPurchaseAccounts();

        // To call PurReturn Create & Update API and To Redirect the PurReturn List Page
        createUpdateCall();

        // Responsive of Common Product Row table
        responsiveCmpTable();

        // RoundOff Calculations Method
        chbPurRtnRoundOff.setOnAction(e -> {
            roundOffCalculations();
        });
        tfInvoiceDateValidation();

        // Add Charges Field Focus Disable and Editable False
        tfPurchaseRtnAddChgAmt.setEditable(false);
        tfPurchaseRtnAddChgAmt.setFocusTraversable(false);
        purRtnLogger.debug("Opened Ledger Modal:On Clicking on by selecting Ledger Name and " + "Select the Ledger from the Modal");
        tfPurRtnLedgName.setOnMouseClicked(actionEvent -> {
            stage = (Stage) bpPurRtnRootPane.getScene().getWindow();
            SingleInputDialogs.openLedgerPopUp(stage, "Creditors", input -> {
                        purRtnLogger.debug("Callback to Get all the Details of the Selected ledger and Set it in the Fields");
                        ledgerName = (String) input[0];
                        ledgerId = (String) input[1];
                        gstDetails = (ObservableList<GstDetailsDTO>) input[2];
                        ledgerStateCode = (String) input[3];
                        tfPurRtnLedgName.setText(ledgerName);
                        tvPurInvCreateCMPTRow.getItems().get(0).setLedger_id(ledgerId);
                        setLedgerGSTDATA(gstDetails);
                        if (ledgerId != null && !ledgerId.isEmpty()) {
                            SingleInputDialogs.ledId = ledgerId;
                        } else {
                            SingleInputDialogs.ledId = "";
                        }

                        tranxLedgerDetailsFun(ledgerId);
                        @SuppressWarnings("unchecked")
                        ObservableList<GstDetailsDTO> gstDetailsDTOS = (ObservableList<GstDetailsDTO>) input[2];

                        for (GstDetailsDTO gstDetailsDTO : gstDetailsDTOS) {
                            supplierGSTINList.add(new GstDetailsDTO(gstDetailsDTO.getGstNo(), gstDetailsDTO.getId(), gstDetailsDTO.getState()));
                        }
                        System.out.println(" supplierGSTINList >> : " + supplierGSTINList.size());
                        if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
                            cmbPurRtnLedgerGst.setItems(supplierGSTINList);
                            cmbPurRtnLedgerGst.setValue(supplierGSTINList.get(0));
                            if (supplierGSTINList.size() > 1) {
                                cmbPurRtnLedgerGst.requestFocus();
                            } else {
                                tfPurRtnInvNum.requestFocus();
                            }
                        } else {
                            tfPurRtnInvNum.requestFocus();
                        }

                        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
                            taxFlag = true;
                        }
                        getBillbyBillWindow(stage, ledgerId);
                        purRtnLogger.debug("Bill by Bill by Window --> Opened Ledger Bill Modal:On Clicking by " + "selecting the Ledger Name ");
                    },
                    input -> {
                        if (input == true) {
                            isLedgerRed = true;
                            setPurChallDataToProduct();
                        }
                    }
            );

        });
        tfPurRtnLedgName.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.SPACE || ev.getCode() == KeyCode.ENTER) {
                try {
                    stage = (Stage) bpPurRtnRootPane.getScene().getWindow();
                    SingleInputDialogs.openLedgerPopUp(stage, "Filter", input -> {

                        purRtnLogger.debug("Callback to Get all the Details of the Selected ledger and Set it in the Fields");
                        ledgerName = (String) input[0];
                        ledgerId = (String) input[1];
                        gstDetails = (ObservableList<GstDetailsDTO>) input[2];
                        ledgerStateCode = (String) input[3];
                        tfPurRtnLedgName.setText(ledgerName);
                        setLedgerGSTDATA(gstDetails);
                        if (ledgerId != null && !ledgerId.isEmpty()) {
                            SingleInputDialogs.ledId = ledgerId;
                        } else {
                            SingleInputDialogs.ledId = "";
                        }
                        tvPurInvCreateCMPTRow.getItems().get(0).setLedger_id(ledgerId);
                        tranxLedgerDetailsFun(ledgerId);
                        @SuppressWarnings("unchecked")
                        ObservableList<GstDetailsDTO> gstDetailsDTOS = (ObservableList<GstDetailsDTO>) input[2];

                        for (GstDetailsDTO gstDetailsDTO : gstDetailsDTOS) {
                            supplierGSTINList.add(new GstDetailsDTO(gstDetailsDTO.getGstNo(), gstDetailsDTO.getId(), gstDetailsDTO.getState()));
                        }
                        System.out.println(" supplierGSTINList >> : " + supplierGSTINList.size());
                        if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
                            cmbPurRtnLedgerGst.setItems(supplierGSTINList);
                            cmbPurRtnLedgerGst.setValue(supplierGSTINList.get(0));
                            if (supplierGSTINList.size() > 1) {
                                cmbPurRtnLedgerGst.requestFocus();
                            } else {
                                tfPurRtnInvNum.requestFocus();
                            }
                        } else {
                            tfPurRtnInvNum.requestFocus();
                        }

                        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
                            taxFlag = true;
                        }
                        getBillbyBillWindow(stage, ledgerId);
                        purRtnLogger.debug("Bill by Bill by Window --> Opened Ledger Bill Modal:On Clicking by " + "selecting the Ledger Name ");
                    }, input -> {
                        if (input == true) {
                            isLedgerRed = true;
                            setPurChallDataToProduct();
                        }
                    });
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    purRtnLogger.error("Exception in opening OpenLedgerPopup():" + exceptionAsString);
                }
            }


        });
    }

    private void getPaymentModes() {
        cmbRtnPaymentMode.setItems(payment_mode_list);
    }


    private void getBillbyBillWindow(Stage stage, String ledgerId) {
        SingleInputDialogs.openInvoiceList(Long.parseLong(ledgerId), stage, "Select Bill", input -> {
            purRtnLogger.debug("Callback to Get all the Details of the Selected ledger and Set it in the Fields");
            invoiceId = input[0].toString();
            getTranxRtnProductList(stage, input[0].toString());
        });
    }

    private void getTranxRtnProductList(Stage stage, String billId) {

        SingleInputDialogs.openTranxRtnProductList(billId, stage, "Select Products", "prtn",(input1, input2) -> {

            purRtnLogger.debug("Callback to get all the Tranx purchase product details of the selected invoice number");
            /**** set api response to the input fields of the invoice data *****/
            int index = 0;
            /**** END *****/
            tvPurInvCreateCMPTRow.getItems().clear();
            Double rowTotalDis = 0.0;
            Double totalTax = 0.0;
            if (input1.size() > 0) {
                for (TranxPurRowResEditDTO mList : input1) {
                    purRtnLogger.debug("For " + mList.getProductName() + " setting up the row value started");
                    System.out.println("For " + mList.getProductName() + " setting up the row value started");
                    newRow = new PurchaseInvoiceTable();
                    try {
                        newRow.setProduct_id("" + mList.getProductId());
                        purRtnLogger.info("ProductName->" + mList.getProductName());
                        System.out.println("ProductName->" + mList.getProductName());
                        newRow.setParticulars(mList.getProductName());
                        purRtnLogger.info("Packing->" + mList.getPacking());
                        System.out.println("Packing->" + mList.getPacking());
                        newRow.setPackages(mList.getPacking());
                        purRtnLogger.info("Unit->" + mList.getUnitName());
                        System.out.println("Unit->" + mList.getUnitName());
                        newRow.setUnit(mList.getUnitName());
                        newRow.setIs_batch("" + mList.getBatch());
                        purRtnLogger.info("Batch No->" + mList.getBatchNo());
                        System.out.println("Batch No->" + mList.getBatchNo());
                        newRow.setBatch_or_serial(mList.getBatchNo());
                        purRtnLogger.info("Qty->" + mList.getQty());
                        System.out.println("Qty->" + mList.getQty());
                        newRow.setQuantity("" + mList.getQty());
                        purRtnLogger.info("Rate->" + mList.getRate());
                        System.out.println("Rate->" + mList.getRate());
                        newRow.setRate("" + mList.getRate());
                        purRtnLogger.info("Gross Amt->" + (mList.getRate() * mList.getQty()));
                        System.out.println("Gross Amt->" + (mList.getRate() * mList.getQty()));
                        newRow.setGross_amount("" + (mList.getRate() * mList.getQty()));
                        purRtnLogger.info("Dis Per->" + mList.getDisPer());
                        System.out.println("Dis Per->" + mList.getDisPer());
                        newRow.setDis_per("" + mList.getDisPer());
                        purRtnLogger.info("Dis Amt->" + mList.getDisAmt());
                        System.out.println("Dis Amt->" + mList.getDisAmt());
                        newRow.setDis_amt("" + mList.getDisAmt());
                        purRtnLogger.info("GST->" + mList.getDisAmt());
                        System.out.println("GST->" + mList.getDisAmt());
                        newRow.setGst("" + mList.getGst());
                        purRtnLogger.info("Final Amt->" + mList.getFinalAmt());
                        System.out.println("Final Amt->" + mList.getFinalAmt());
                        newRow.setNet_amount("" + mList.getFinalAmt());
                        purRtnLogger.info("Free Qty->" + mList.getFreeQty());
                        System.out.println("Free Qty->" + mList.getFreeQty());
                        newRow.setFree(String.valueOf(mList.getFreeQty()));
                        purRtnLogger.info("IGST ->" + mList.getIgst());
                        System.out.println("IGST ->" + mList.getIgst());
                        newRow.setIgst("" + mList.getIgst());
                        purRtnLogger.info("SGST ->" + mList.getSgst());
                        System.out.println("SGST ->" + mList.getSgst());
                        newRow.setSgst("" + mList.getSgst());
                        purRtnLogger.info("CGST ->" + mList.getCgst());
                        System.out.println("CGST ->" + mList.getCgst());
                        newRow.setCgst("" + mList.getCgst());
                        purRtnLogger.info("Total IGST ->" + mList.getTotalIgst());
                        System.out.println("Total IGST ->" + mList.getTotalIgst());
                        newRow.setTotal_igst("" + mList.getTotalIgst());
                        purRtnLogger.info("Total SGST ->" + mList.getTotalSgst());
                        System.out.println("Total SGST ->" + mList.getTotalSgst());
                        newRow.setTotal_sgst("" + mList.getTotalSgst());
                        purRtnLogger.info("Total CGST ->" + mList.getTotalCgst());
                        System.out.println("Total CGST ->" + mList.getTotalCgst());
                        newRow.setTotal_cgst("" + mList.getTotalCgst());
                        purRtnLogger.info("Tax Per ->" + mList.getGst());
                        System.out.println("Tax Per ->" + mList.getGst());
                        newRow.setTax(String.valueOf(mList.getGst()));
                        purRtnLogger.info("Taxable Amt ->" + mList.getGrossAmt());
                        System.out.println("Taxable Amt ->" + mList.getGrossAmt());
                        newRow.setTaxable_amt("" + mList.getGrossAmt());
                        System.out.println("Row Discount--->" + mList.getRowDisAmt());
                        newRow.setLevelA_id(String.valueOf(mList.getLevelAId()));
                        // newRow.setLevelA("");
                        newRow.setLevelB_id(String.valueOf(mList.getLevelBId()));
                        //newRow.setLevelB("");
                        newRow.setLevelC_id(String.valueOf(mList.getLevelCId()));
                        // newRow.setLevelC("");
                        newRow.setUnit_id(String.valueOf(mList.getUnitId()));
                        newRow.setUnit_conv(String.valueOf(mList.getUnitConv()));
                        newRow.setReference_id(billId);
                        newRow.setReference_type("pur_invoice");
                        rowTotalDis += mList.getRowDisAmt();
                        newRow.setFinal_dis_amt("" + rowTotalDis);
                        System.out.println("total final Discount--->" + rowTotalDis);
                        //calculate tax per and store the data to final net AMount
                        if (mList.getGst() > 0) {
                            Double total_tax_amt = (mList.getGst() / 100) * (mList.getGrossAmt());
                            totalTax += total_tax_amt;
                            System.out.println("row total  Tax-->" + totalTax);
                            newRow.setFinal_tax_amt("" + total_tax_amt);
                        }
                        //  tvPurInvCreateCMPTRow.getItems().add(newRow);
                        BatchWindowTableDTO batchWindowTableDTO = new BatchWindowTableDTO();
                        batchWindowTableDTO.setProductId(newRow.getProduct_id());
                        batchWindowTableDTO.setLevelAId(newRow.getLevelA_id());
                        batchWindowTableDTO.setLevelBId(newRow.getLevelB_id());
                        batchWindowTableDTO.setLevelCId(newRow.getLevelC_id());
                        batchWindowTableDTO.setUnitId(newRow.getUnit_id());
                        batchWindowTableDTO.setBatchNo(newRow.getBatch_or_serial());
                        batchWindowTableDTO.setB_details_id(newRow.getB_details_id());

                        if (newRow.getManufacturing_date() != null && !newRow.getManufacturing_date().isEmpty()) {
                            LocalDate manu_dt = LocalDate.parse(newRow.getManufacturing_date());
                            batchWindowTableDTO.setManufacturingDate(manu_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } else {
                            batchWindowTableDTO.setManufacturingDate("");
                        }

                        if (newRow.getB_expiry() != null && !newRow.getB_expiry().isEmpty()) {
                            LocalDate exp_dt = LocalDate.parse(newRow.getB_expiry());
                            batchWindowTableDTO.setExpiryDate(exp_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        } else {
                            batchWindowTableDTO.setExpiryDate("");
                        }

                        batchWindowTableDTO.setMrp(newRow.getB_rate());
                        batchWindowTableDTO.setPurchaseRate(newRow.getB_purchase_rate());

                        batchWindowTableDTO.setQuantity(newRow.getQuantity());
                        batchWindowTableDTO.setFree(newRow.getFree());

                        batchWindowTableDTO.setDiscountPercentage(newRow.getDis_per());
                        batchWindowTableDTO.setDiscountAmount(newRow.getDis_amt());

                        //batchWindowTableDTO.setBarcode(newRow.getBarcode());
                        batchWindowTableDTO.setMargin(newRow.getMin_margin());


                        batchWindowTableDTO.setCsr_mh(newRow.getRate_c());
                        batchWindowTableDTO.setCsr_ai(newRow.getRate_d().equals("0.0") ? "" : newRow.getRate_d());
                        batchWindowTableDTO.setFsr_mh(newRow.getRate_a());
                        batchWindowTableDTO.setFsr_ai(newRow.getRate_b());

                        batchWindowTableDTO.setSupplier_id(newRow.getLedger_id());
                        batchWindowTableDTO.setTax(newRow.getTax());

                        List<BatchWindowTableDTO> listBatch = new ArrayList<>();
                        listBatch.add(batchWindowTableDTO);
                        newRow.setBatchWindowTableDTOList(listBatch);

                        // newRowList.add(newRow);
                        tvPurInvCreateCMPTRow.getItems().addAll(newRow);
                        PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(index, tvPurInvCreateCMPTRow, callback);
                        PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
                        List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPackingRtn.getAllProductUnitsPackingRtnFlavour(newRow.getProduct_id());

                        ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);

                        if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                            PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                            for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                                if (tvPurInvCreateCMPTRow.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                                }
                            }

                        } else {
                            PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);

                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                            for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                                if (tvPurInvCreateCMPTRow.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                                }
                            }
                        }
                        index++;
                        purRtnLogger.debug("For " + mList.getProductName() + " setting up the row value ended");
                        System.out.println("For " + mList.getProductName() + " setting up the row value ended");
                    } catch (Exception e) {
                        System.out.println("Exception---->" + e.getMessage());
                    }
                }
            } else {
                tfPurRtnLedgName.requestFocus();
            }
            //tvPurInvCreateCMPTRow.refresh();
            if (input2 != null) {
                tfPurRtnDisPer.setText("" + input2.getDiscountInPer());
                tfPurRtnDisAmt.setText("" + input2.getDiscountInAmt());
            } else {
                tfPurRtnDisPer.setText("0.0");
                tfPurRtnDisAmt.setText("0.0");
            }
        });
    }


    private void setLedgerGSTDATA(ObservableList<GstDetailsDTO> gstDetails) {
        if (gstDetails != null) {

            cmbPurRtnLedgerGst.setItems(gstDetails);
            cmbPurRtnLedgerGst.setConverter(new StringConverter<GstDetailsDTO>() {
                @Override
                public String toString(GstDetailsDTO gstDetailsDTO) {
                    if (gstDetailsDTO != null) return gstDetailsDTO.getGstNo();
                    else return "";
                }

                @Override
                public GstDetailsDTO fromString(String s) {
                    return null;
                }
            });
            GstDetailsDTO selectedGst = null;
            for (Object obj : cmbPurRtnLedgerGst.getItems()) {
                GstDetailsDTO gstDetailsDTO = (GstDetailsDTO) obj;
                if (gstDetailsDTO.getId().equals(gstDetails.get(0).getId())) {
                    selectedGst = gstDetailsDTO;
                    break;
                }
            }
            if (selectedGst != null) {
                cmbPurRtnLedgerGst.getSelectionModel().select(selectedGst);
            }
        }
    }


    //? On Clicking on the Modify Button From Create or Edit page redirect Back to List page
    public void backToListModify() {
        purRtnEditId = null;
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure want to go list ?", input -> {
            if (input == 1) {
                SingleInputDialogs.productId = "";
                SingleInputDialogs.ledId = "";
                GlobalController.getInstance().addTabStatic(PURCHASE_RETURN_LIST_SLUG, false);
            }
        });

    }

    // On Clicking on the Cancel Button From Create or Edit page redirect Back to List page.
    public void backToList() {
        purRtnEditId = null;
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure want to Cancel ?", input -> {
            if (input == 1) {
                clearLedAndProdInfo();
            }
        });
    }

    public void clearLedAndProdInfo() {
//        ObservableList<SalesQuotationProductDTO> emptyList = FXCollections.observableArrayList();
//        tvSalesOrderCmpTRow.setItems(emptyList);
        ObservableList<SalesOrderSupplierDetailsDTO> emptyList1 = FXCollections.observableArrayList();
//        Debtor_id = "";
        tfPurRtnLedgName.setText("");
        cmbPurRtnLedgerGst.getSelectionModel().clearSelection();
//        tvSalesQuotCreateprodInvoiceDetails.setItems(emptyList1);
        tfPurRtnNarrtion.setText("");
        //to Focus on Selected Product passed ProductId. on Clear pass blank
        SingleInputDialogs.productId = "";
        SingleInputDialogs.ledId = "";

        //? clear Ledger Info
        txtPurRtnGstNo.setText("");
        txtPurRtnArea.setText("");
        txtPurRtnBank.setText("");
        txtPurRtnContactPerson.setText("");
        txtPurRtnFssai.setText("");
        txtPurRtnLicenseNo.setText("");
        txtPurRtnRoute.setText("");
        txtPurRtnTransportName.setText("");
        txtPurRtnCreditDays.setText("");

        //? clear Product Info
        txtPurRtnProductBrand.setText("");
        txtPurRtnProductGroup.setText("");
        txtPurRtnProductSubGroup.setText("");
        txtPurRtnProductCategory.setText("");
        txtPurRtnProductHsn.setText("");
        txtPurRtnProductTaxType.setText("");
        txtPurRtnProductTaxPer.setText("");
        txtPurRtnProductMarginPer.setText("");
        txtPurRtnProductCost.setText("");
        txtPurRtnProductShelfId.setText("");
        txtPurRtnProductMaxStock.setText("");
        txtPurRtnProductMinStock.setText("");

        //clear all row calculation
        tfPurRtnDisPer.setText("");

        tfPurRtnDisAmt.setText("");

        lblPurRtnGrossTotal.setText("0.00");

        lblPurRtnTotalDiscount.setText("0.00");

        lblPurRtnTotalTaxableAmount.setText("0.00");
        lblPurRtnTotalTax.setText("0.00");
        lblPurRtnTotalBillAmount.setText("0.00");

        tvPurInvCreateCMPTRow.getItems().clear();// Add a new blank row if needed
        tvPurInvCreateCMPTRow.getItems().add(new PurchaseInvoiceTable("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        tfPurRtnLedgName.requestFocus();
        tvPurRtnSupplierDetails.getItems().clear();
        PurInvoiceCommunicator.resetFields();//new 2.0 to Clear the CMPTROW ComboBox

    }

    private void resposiveScreenCss() {
        double widthInParent = tranxDateLabel.getBoundsInParent().getWidth();
        System.out.println("Label width in parent: " + widthInParent);
        // For Responsive Resolutions Css Style Apply
        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);
        if (width >= 992 && width <= 1024) {
            topFirstHboxId.setSpacing(10);
            topSecondHboxId.setSpacing(10);
            pcTotalMainDiv.setSpacing(10);
            pcTotalMainInnerDiv.setSpacing(8);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.53));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.25));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
//            so2cBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_992_1024.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_992_1024.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle1.css").toExternalForm());
        } else if (width >= 1025 && width <= 1280) {
            topFirstHboxId.setSpacing(10);
            topSecondHboxId.setSpacing(10);
            pcTotalMainDiv.setSpacing(10);
            pcTotalMainInnerDiv.setSpacing(8);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.56));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
//            so2cBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_1025_1280.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_1025_1280.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle2.css").toExternalForm());
        } else if (width >= 1281 && width <= 1368) {
            topFirstHboxId.setSpacing(12);
            topSecondHboxId.setSpacing(12);
            pcTotalMainDiv.setSpacing(12);
            pcTotalMainInnerDiv.setSpacing(10);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.6));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.20));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.20));
//            so2cBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_1281_1368.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_1281_1368.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle3.css").toExternalForm());
        } else if (width >= 1369 && width <= 1400) {
            topFirstHboxId.setSpacing(15);
            topSecondHboxId.setSpacing(15);
            pcTotalMainDiv.setSpacing(15);
            pcTotalMainInnerDiv.setSpacing(10);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.57));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.21));
//            so2cBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_1369_1400.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_1369_1400.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle4.css").toExternalForm());
        } else if (width >= 1401 && width <= 1440) {
            topFirstHboxId.setSpacing(15);
            topSecondHboxId.setSpacing(15);
            pcTotalMainDiv.setSpacing(15);
            pcTotalMainInnerDiv.setSpacing(10);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.57));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.21));
//            so2cBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_1401_1440.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_1401_1440.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle5.css").toExternalForm());
        } else if (width >= 1441 && width <= 1680) {
            topFirstHboxId.setSpacing(20);
            topSecondHboxId.setSpacing(20);
            pcTotalMainDiv.setSpacing(20);
            pcTotalMainInnerDiv.setSpacing(10);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.6));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.20));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.20));
//            scBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/gst_table_css/gst_table_1441_1680.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_challan_css/supplier_table_css/supplier_table_1441_1680.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle6.css").toExternalForm());
        } else if (width >= 1681 && width <= 1920) {
            topFirstHboxId.setSpacing(20);
            topSecondHboxId.setSpacing(20);
            pcTotalMainDiv.setSpacing(20);
            pcTotalMainInnerDiv.setSpacing(10);
            // Set preferred widths as percentages of the HBox width
            pcBottomFirstV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.6));
            pcBottomSecondV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.18));
            pcBottomThirdV.prefWidthProperty().bind(pcBottomMain.widthProperty().multiply(0.22));
//            scBottomMain.setPrefHeight(150);
            tblvPurRtnGST.getStylesheets().add(GenivisApplication.class.getResource("ui/css/gst_table.css").toExternalForm());
            tvPurRtnSupplierDetails.getStylesheets().add(GenivisApplication.class.getResource("ui/css/invoice_product_history_table.css").toExternalForm());
            bpPurRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle7.css").toExternalForm());

        }
    }

    private void tfInvoiceDateValidation() {
        tfPurRtnInvDate.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfPurRtnInvDate.getText().isEmpty()) {
                    tfPurRtnInvDate.requestFocus();
                } else {
                    validateInvoiceDate();
                }
            }
        });
    }

    private void validateInvoiceDate() {
        try {
            purRtnLogger.debug("Initiate the validateInvoiceDate() method");
            Map<String, String> map = new HashMap<>();
            String invoiceDate = Communicator.text_to_date.fromString(tfPurRtnInvDate.getText()).toString();
            map.put("invoiceDate", invoiceDate);
            String formData = Globals.mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.VALIDATE_PUR_INV_DATE);
            String resBody = response.body();
            JsonObject obj = new Gson().fromJson(resBody, JsonObject.class);
            if (obj.get("responseStatus").getAsInt() == 404) {
                AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeWarning, obj.get("message").getAsString(), callback -> {
                    purRtnLogger.debug("Invoice Date : " + invoiceDate + " not in the Financial Year in Pur Return");
                    purRtnLogger.debug("END of validateInvoiceDate() method in Pur Return");
                });
                tfPurRtnInvDate.requestFocus();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purRtnLogger.error("Exception in validateInvoiceDate:" + exceptionAsString);
        }

    }

    public void tfLedgerNameValidation() {
        tfPurRtnLedgName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfPurRtnLedgName.getText().isEmpty()) {
                    tfPurRtnLedgName.requestFocus();
                }
            }
        });
    }

    private void dateSetValidation() {
        DateValidator.applyDateFormat(tfPurInvCreateTranxDate);
        DateValidator.applyDateFormat(tfPurRtnInvDate);

        // Set the current date to the TranxDate and Return Date
        LocalDate currDate = LocalDate.now();
        System.out.println("invoice Edit Id" + purRtnEditId);
        System.out.println("Tranx Dt:" + invoice_data_map.get("transaction_dt"));

        if (purRtnEditId > 0) {
            LocalDate transaction_dt = LocalDate.parse(invoice_data_map.get("transaction_dt"));
            tfPurInvCreateTranxDate.setText(transaction_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            LocalDate invoice_dt = LocalDate.parse(invoice_data_map.get("invoice_dt"));
            tfPurRtnInvDate.setText(invoice_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            tfPurInvCreateTranxDate.setText(currDate.format(formatter));
            tfPurRtnInvDate.setText(currDate.format(formatter));
            sceneInitilization();
        }
    }

    private void discPropDataSetInEdit() {
        if (purRtnEditId > 0) {
            System.out.println("edit_response_map.get(\"discountInPer\") >> " + edit_response_map.get("discountInPer"));
            Double dis_per_float = (Double) edit_response_map.get("discountInPer");
            tfPurRtnDisPer.setText(String.valueOf(dis_per_float));

            String discText = tfPurRtnDisPer.getText();
            if (!TextUtils.isEmpty(discText)) {
                double totalTaxableAmt = 0.0;
                for (int i = 0; i < tvPurInvCreateCMPTRow.getItems().size(); i++) {
                    System.out.println("Gross amt in Prop--->" + tvPurInvCreateCMPTRow.getItems().get(i).getGross_amount());
                    PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(i, tvPurInvCreateCMPTRow, callback);//call row calculation function
                    PurchaseInvoiceTable purchaseInvoiceTable = tvPurInvCreateCMPTRow.getItems().get(i);
                    totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());

                }
                double disc_per = Double.parseDouble(discText);
                System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
                Double amount = (totalTaxableAmt * disc_per) / 100;
                tfPurRtnDisAmt.setText(String.valueOf(amount));
                PurchaseInvoiceCalculationRtn.discountPropotionalCalculation(disc_per + "", "0", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
                if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
                }
            } else {
                tfPurRtnDisAmt.setText("");
                PurchaseInvoiceCalculationRtn.discountPropotionalCalculation("0", "0", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
                if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
                }
            }

            //? Calculate Tax for each Row
            PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);


        }
    }

    public static void showDropDownOnKeyPress(ComboBox<?> comboBox) {
        comboBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.DOWN) {
                comboBox.show();
            }
        });
    }

    public void responsiveCmpTable() {
        tblcPurInvCreateCmptSrNo.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.02));
        tblcPurInvCreateCmptPackage.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        tblcPurInvCreateCmptUnit.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        tblcPurInvCreateCmptQuantity.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        tblcPurInvCreateCmptDiscPerc.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        tblcPurInvCreateCmptDiscAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        tblcPurInvCreateCmptTaxPerc.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.04));
        if (tblcPurInvCreateCmptFreeQty.isVisible()) {
            tblcPurInvCreateCmptFreeQty.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.04));
            tblcPurInvCreateCmptRate.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
        } else {
            tblcPurInvCreateCmptRate.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.08));
        }
        if (tblcPurInvCreateCmptLevelA.isVisible() == true && tblcPurInvCreateCmptLevelB.isVisible() == false && tblcPurInvCreateCmptLevelC.isVisible() == false) {
            tblcPurInvCreateCmptParticular.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.29));
            tblcPurInvCreateCmptGrossAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.1));
            tblcPurInvCreateCmptNetAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.07));
            tblcPurInvCreateCmptBatch.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.1));
            tblcPurInvCreateCmptLevelA.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
        } else if (tblcPurInvCreateCmptLevelA.isVisible() == true && tblcPurInvCreateCmptLevelB.isVisible() == true && tblcPurInvCreateCmptLevelC.isVisible() == false) {
            tblcPurInvCreateCmptLevelA.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
            tblcPurInvCreateCmptParticular.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.28));
            tblcPurInvCreateCmptGrossAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.08));
            tblcPurInvCreateCmptNetAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
            tblcPurInvCreateCmptBatch.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
            tblcPurInvCreateCmptLevelB.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        } else if (tblcPurInvCreateCmptLevelA.isVisible() == true && tblcPurInvCreateCmptLevelB.isVisible() == true && tblcPurInvCreateCmptLevelC.isVisible() == true) {
            tblcPurInvCreateCmptParticular.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.245));
            tblcPurInvCreateCmptGrossAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.07));
            tblcPurInvCreateCmptNetAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
            tblcPurInvCreateCmptBatch.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.06));
            tblcPurInvCreateCmptLevelA.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
            tblcPurInvCreateCmptLevelB.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
            tblcPurInvCreateCmptLevelC.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.05));
        } else {
            tblcPurInvCreateCmptParticular.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.30));
            tblcPurInvCreateCmptGrossAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.08));
            tblcPurInvCreateCmptNetAmt.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.08));
            tblcPurInvCreateCmptBatch.prefWidthProperty().bind(tvPurInvCreateCMPTRow.widthProperty().multiply(0.08));
        }

    }

    private void gstTableDesign() {
        tblvPurRtnGST.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblcPurRtnGST.setCellValueFactory(cellData -> {
            StringProperty taxProperty = cellData.getValue().taxPerProperty();
            return Bindings.createStringBinding(
                    () -> "(" + (int) Double.parseDouble(taxProperty.get()) + " %)",
                    taxProperty
            );
        });

        tblcPurRtnGST.setStyle("-fx-alignment: CENTER;");
        tblcPurRtnCGST.setCellValueFactory(cellData -> cellData.getValue().cgstProperty());
        tblcPurRtnCGST.setCellFactory(col -> new TableCell<GstDTO, String>() {
            private final Label label = new Label();
            private VBox vBox = new VBox(label);

            @Override
            protected void updateItem(String item, boolean empty) {
                //label.setStyle("-fx-font-family: Arial; -fx-font-size: 16px;");
                vBox.setAlignment(Pos.CENTER);
                vBox.setMaxHeight(30);
                vBox.setMinHeight(30);
                vBox.setPrefHeight(30);
                vBox.setStyle("-fx-border-color: #DCDCDC; -fx-background-color: #EBEBEB; -fx-background-radius: 3; -fx-border-width: 1px; -fx-border-radius: 3;");
                vBox.setAlignment(Pos.CENTER);
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item);
                    // Add border with width
                    vBox.setStyle("-fx-border-color: #DCDCDC; -fx-background-color: #EBEBEB; -fx-background-radius: 3; -fx-border-width: 1px; -fx-border-radius: 3;");


                    setGraphic(vBox);
                }
            }
        });


        tblcPurRtnCGST.setStyle("-fx-alignment: CENTER;");
        tblcPurRtnSGST.setCellValueFactory(cellData -> cellData.getValue().sgstProperty());
        tblcPurRtnSGST.setStyle("-fx-alignment: CENTER;");
        tblcPurRtnSGST.setCellFactory(col -> new TableCell<GstDTO, String>() {
            private final Label label = new Label();
            private VBox vBox = new VBox(label);

            @Override
            protected void updateItem(String item, boolean empty) {
                //label.setStyle("-fx-font-family: Arial; -fx-font-size: 16px;");
                vBox.setAlignment(Pos.CENTER);
                vBox.setMaxHeight(30);
                vBox.setMinHeight(30);
                vBox.setPrefHeight(30);
                vBox.setStyle("-fx-border-color: #DCDCDC; -fx-background-color: #EBEBEB; -fx-background-radius: 3; -fx-border-width: 1px; -fx-border-radius: 3;");

                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(item);
                    // Add border with width
                    setGraphic(vBox);
                }
            }
        });
    }

    private void initialEnterMethod() {
        bpPurRtnRootPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
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
            } else if (event.getCode() == KeyCode.X && event.isControlDown()) {
                btnPurRtnCancel.fire();
            } else if (event.getCode() == KeyCode.E && event.isControlDown()) {
                btnPurRtnFinalModify.fire();
            } else if (event.getCode() == KeyCode.S && event.isControlDown()) {
                btnPurRtnFinalSubmit.fire();
            }

        });
    }

    private void createUpdateCall() {
        btnPurRtnFinalSubmit.setOnAction((event) -> {

            if (tfPurRtnLedgName.getText().isEmpty()) {
                AlertUtility.AlertErrorTimeout(AlertUtility.alertTypeError, "Enter Mandatory Ledger Name Field", input -> {
                    if (input == 1) {
                        tfPurRtnLedgName.requestFocus();
                    }
                });
            } else if (tfPurRtnInvNum.getText().isEmpty()) {
                AlertUtility.AlertErrorTimeout(AlertUtility.alertTypeError, "Enter Mandatory Return No Field", input -> {
                    if (input == 1) {
                        tfPurRtnLedgName.requestFocus();
                    }
                });
            } else {
                String btnText = btnPurRtnFinalSubmit.getText();
                if (btnText.equalsIgnoreCase("Submit")) {
                    if (purchaseOrderDTO == null) {
                        //Normal Purchase Return Create
                        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation,
                                LedgerMessageConsts.msgConfirmationOnSubmit + tfPurRtnInvNum.getText(), input -> {
                                    if (input == 1) {
                                        createPurchaseReturnNew();
                                    }
                                });
                    }
                } else {
                    // PurchaseRetunUpdate API Call
                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation,
                            LedgerMessageConsts.msgConfirmationOnUpdate + tfPurRtnInvNum.getText(), input -> {
                                if (input == 1) {
                                    createPurchaseReturnNew();
                                }
                            });
                }
            }

        });

    }

    TableCellCallback<Object[]> callback = item -> {

        System.out.println("item: " + item[0]);
        System.out.println("item: " + item[1]);
        System.out.println("item: " + item[2]);
        System.out.println("item: " + item[3]);
        System.out.println("item: " + item[4]);
        System.out.println("item: " + item[5]);
        System.out.println("item: " + item[6]);
        System.out.println("item: " + item[7]);
        System.out.println("item: " + item[8]);

        System.out.println("taxCalculation " + item[9]);

        tblvPurRtnGST.getItems().clear();
        ObservableList<GstDTO> gstDTOObservableList = FXCollections.observableArrayList();
        ObservableList<?> item9List = (ObservableList<?>) item[9];
        for (Object obj : item9List) {
            if (obj instanceof GstDTO) {
                gstDTOObservableList.add((GstDTO) obj);
            }
        }
        tblvPurRtnGST.getItems().addAll(gstDTOObservableList);

        JsonArray jsonArray = new JsonArray();
        JsonObject jsonResult = new JsonObject();
        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
            for (GstDTO gstDTO : gstDTOObservableList) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("d_gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer())));
                jsonObject.addProperty("gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer()) / 2));
                jsonObject.addProperty("amt", gstDTO.getCgst());
                jsonArray.add(jsonObject);
            }
            jsonResult.add("cgst", jsonArray);
            jsonResult.add("sgst", jsonArray);
        } else {
            for (GstDTO gstDTO : gstDTOObservableList) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("d_gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer())));
                jsonObject.addProperty("gst", decimalFormat.format(Double.parseDouble(gstDTO.getTaxPer())));
                jsonObject.addProperty("amt", gstDTO.getCgst());
                jsonArray.add(jsonObject);
            }
            jsonResult.add("igst", jsonArray);
        }
        purchase_return_map.put("taxCalculation", jsonResult.toString());


        totalamt = (String) item[2];


        total_purchase_discount_amt = (String) item[4];


        purchase_return_map.put("total_qty", (String) item[0]);
        purchase_return_map.put("total_free_qty", (String) item[1]);

        purchase_return_map.put("total_base_amt", (String) item[3]);
        purchase_return_map.put("total_invoice_dis_amt", (String) item[4]);
        purchase_return_map.put("taxable_amount", (String) item[5]);
        purchase_return_map.put("bill_amount", (String) item[2]);
        purchase_return_map.put("total_tax_amt", (String) item[6]);

        purchase_return_map.put("totaligst", (String) item[6]);
        purchase_return_map.put("totalsgst", (String) item[7]);
        purchase_return_map.put("totalcgst", (String) item[8]);


        System.out.println("callBack TotalTaxableAMount :: >> " + Double.parseDouble((String) item[5]));
        total_taxable_amt = Double.parseDouble((String) item[5]);
        lblPurRtnTotalQty.setText((String) item[0]);
        lblPurRtnFreeQty.setText((String) item[1]);
        lblPurRtnGrossTotal.setText((String) item[3]);
        lblPurRtnTotalDiscount.setText((String) item[4]);
        lblPurRtnTotalTaxableAmount.setText((String) item[5]);
        lblPurRtnTotalTax.setText((String) item[6]);
        lblPurRtnTotalCGST.setText((String) item[7]);
        lblPurRtnTotalSGST.setText((String) item[8]);

        /**
         * Info : RoundOff Calculation Method
         * @Author : Vinit Chilaka
         */
        chbPurRtnRoundOff.setSelected(true);
        roundOffCalculations();

        hbPurRtnGstTotal.setVisible(true);

    };
    TableCellCallback<Object[]> delCallback = item -> {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("del_id", item[0].toString());
        deletedRows.add(jsonObject);
    };


    private void roundOffCalculations() {
        Double billamt = 0.00; // local variable to store row wise  Bill amount
        Double total_bill_amt = 0.00; // local variable to store Final bill Amount without roundoff
        Double final_r_off_bill_amt = 0.00; // local variable to store Final Bill amount with round off
        Double roundOffAmt = 0.00; // local variable to store Roundoff Amount
        for (PurchaseInvoiceTable cmpTRowDTO : tvPurInvCreateCMPTRow.getItems()) {
            billamt = cmpTRowDTO.getNet_amount().isEmpty() ? 0.0 : Double.valueOf(cmpTRowDTO.getNet_amount());
            total_bill_amt += billamt;
        }
        final_r_off_bill_amt = (double) Math.round(total_bill_amt);
        roundOffAmt = final_r_off_bill_amt - total_bill_amt;

        if (chbPurRtnRoundOff.isSelected()) {
            lblPurRtnTotalBillAmount.setText(String.format("%.2f", final_r_off_bill_amt));
            lblPurRtnRoundOff.setText(String.format("%.2f", roundOffAmt));
        } else {
            lblPurRtnTotalBillAmount.setText(String.format("%.2f", total_bill_amt));
            lblPurRtnRoundOff.setText("0.00");
        }

    }


    TableCellCallback<Object[]> productID_callback = item -> {
        if (item.length == 1) {
            tranxProductDetailsFun((String) item[0]);
            getSupplierListbyProductId((String) item[0]);
        } else {
            tranxBatchDetailsFun((String) item[0], (String) item[1]);
        }
    };

    TableCellCallback<Object[]> addPrdCalbak = item -> {
        if (item != null) {
            System.out.println("item [0] " + item[0]);
            System.out.println("item [0] " + item[1]);
            if ((Boolean) item[0] == true) {
                rediCurrIndex = (Integer) item[1];
                String isProduct = (String) item[2];
                if (isProduct.equalsIgnoreCase("isProduct")) {
                    isProductRed = true;
                }
                setPurChallDataToProduct();
            }
            System.out.println("isProductRed >> " + isProductRed);
        }
    };


    public void tableInitiliazation() {

        tvPurInvCreateCMPTRow.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tvPurInvCreateCMPTRow.setEditable(true);
        tvPurInvCreateCMPTRow.setFocusTraversable(false);

        Label headerLabel = new Label("Sr\nNo.");
        tblcPurInvCreateCmptSrNo.setGraphic(headerLabel);

        if (purRtnEditId > 0) {

            int count = 1;
            int index = 0;
            for (TranxPurRowResEditDTO rowListDTO : rowListDTOS) {
                PurchaseInvoiceTable purchaseInvoiceTable = new PurchaseInvoiceTable();
                purchaseInvoiceTable.setPurchase_id(String.valueOf(purRtnEditId));
                purchaseInvoiceTable.setSr_no(String.valueOf(count));
                purchaseInvoiceTable.setDetails_id(String.valueOf(rowListDTO.getDetailsId()));
                purchaseInvoiceTable.setProduct_id(String.valueOf(rowListDTO.getProductId()));

                //to Focus on Selected Product passed ProductId
                if (rowListDTO.getProductId() != null && !rowListDTO.getProductId().equals("")) {
                    SingleInputDialogs.productId = String.valueOf(rowListDTO.getProductId());
                }
                purchaseInvoiceTable.setParticulars(String.valueOf(rowListDTO.getProductName()));

                purchaseInvoiceTable.setLevelA_id(String.valueOf(rowListDTO.getLevelAId()));
                purchaseInvoiceTable.setLevelB_id(String.valueOf(rowListDTO.getLevelBId()));
                purchaseInvoiceTable.setLevelC_id(String.valueOf(rowListDTO.getLevelCId()));
                purchaseInvoiceTable.setUnit_id(String.valueOf(rowListDTO.getUnitId()));
                purchaseInvoiceTable.setUnit_conv(String.valueOf(rowListDTO.getUnitConv()));

                purchaseInvoiceTable.setPackages(String.valueOf(rowListDTO.getPackName()));

                purchaseInvoiceTable.setQuantity(String.valueOf(rowListDTO.getQty()));
                purchaseInvoiceTable.setRate(String.valueOf(rowListDTO.getRate()));
                purchaseInvoiceTable.setBase_amt(String.valueOf(rowListDTO.getBaseAmt()));

                purchaseInvoiceTable.setDis_amt(String.valueOf(rowListDTO.getDisAmt()));
                purchaseInvoiceTable.setDis_per(String.valueOf(rowListDTO.getDisPer()));
                purchaseInvoiceTable.setDis_per_cal(String.valueOf(rowListDTO.getDisPerCal()));
                purchaseInvoiceTable.setDis_amt_cal(String.valueOf(rowListDTO.getDisAmtCal()));
                purchaseInvoiceTable.setTotal_amt(String.valueOf(rowListDTO.getTotalAmt()));


                purchaseInvoiceTable.setGst(String.valueOf(rowListDTO.getGst()));
                purchaseInvoiceTable.setIgst_per(String.valueOf(rowListDTO.getIgst()));
                purchaseInvoiceTable.setSgst_per(String.valueOf(rowListDTO.getSgst()));
                purchaseInvoiceTable.setCgst_per(String.valueOf(rowListDTO.getCgst()));

                purchaseInvoiceTable.setIgst(String.valueOf(rowListDTO.getIgst()));
                purchaseInvoiceTable.setCgst(String.valueOf(rowListDTO.getCgst()));
                purchaseInvoiceTable.setSgst(String.valueOf(rowListDTO.getSgst()));

                purchaseInvoiceTable.setNet_amount(String.valueOf(rowListDTO.getFinalAmt()));
                purchaseInvoiceTable.setFree(String.valueOf(rowListDTO.getFreeQty()));
                purchaseInvoiceTable.setRow_dis_amt(String.valueOf(rowListDTO.getRowDisAmt()));

                purchaseInvoiceTable.setGross_amount(String.valueOf(rowListDTO.getGrossAmt()));
                purchaseInvoiceTable.setGross_amount1(String.valueOf(rowListDTO.getGrossAmt()));

                purchaseInvoiceTable.setAdd_chg_amt(String.valueOf(rowListDTO.getAddChgAmt()));


                purchaseInvoiceTable.setInvoice_dis_amt(String.valueOf(rowListDTO.getInvoiceDisAmt()));
                purchaseInvoiceTable.setInventoryId(String.valueOf(rowListDTO.getInventoryId()));

                purchaseInvoiceTable.setTransaction_status(String.valueOf(rowListDTO.getTransactionStatus()));

                purchaseInvoiceTable.setIs_expired(String.valueOf(rowListDTO.getExpired()));


                purchaseInvoiceTable.setB_details_id(String.valueOf(rowListDTO.getbDetailsId()));

                purchaseInvoiceTable.setB_no(String.valueOf(rowListDTO.getBatchNo()));
                purchaseInvoiceTable.setBatch_or_serial(String.valueOf(rowListDTO.getBatchNo()));
                purchaseInvoiceTable.setB_expiry(String.valueOf(rowListDTO.getbExpiry()));
                purchaseInvoiceTable.setB_rate(String.valueOf(rowListDTO.getbRate()));
                purchaseInvoiceTable.setB_purchase_rate(String.valueOf(rowListDTO.getPurchaseRate()));
                purchaseInvoiceTable.setIs_batch(String.valueOf(rowListDTO.getBatch()));

                purchaseInvoiceTable.setRate_a(String.valueOf(rowListDTO.getMinRateA() == null ? "" : rowListDTO.getMinRateA()));
                purchaseInvoiceTable.setRate_b(String.valueOf(rowListDTO.getMinRateB() == null ? "" : rowListDTO.getMinRateB()));
                purchaseInvoiceTable.setRate_c(String.valueOf(rowListDTO.getMinRateC() == null ? "" : rowListDTO.getMinRateB()));
                // purchaseInvoiceTable.setRate_d(String.valueOf(rowListDTO.getMinRateD()));

                purchaseInvoiceTable.setManufacturing_date(String.valueOf(rowListDTO.getManufacturingDate()));
                purchaseInvoiceTable.setMin_margin(String.valueOf(rowListDTO.getMarginPer()));

                purchaseInvoiceTable.setCosting(String.valueOf(rowListDTO.getCosting()));
                purchaseInvoiceTable.setCosting_with_tax(String.valueOf(rowListDTO.getCosting()));
                purchaseInvoiceTable.setLedger_id(String.valueOf(rowListDTO.getCosting()));
                purchaseInvoiceTable.setLedger_id(invoice_data_map.get("supplierId"));
                purchaseInvoiceTable.setTax(String.valueOf(rowListDTO.getGst()));

                BatchWindowTableDTO batchWindowTableDTO = new BatchWindowTableDTO();


                batchWindowTableDTO.setProductId(purchaseInvoiceTable.getProduct_id());
                batchWindowTableDTO.setLevelAId(purchaseInvoiceTable.getLevelA_id());
                batchWindowTableDTO.setLevelBId(purchaseInvoiceTable.getLevelB_id());
                batchWindowTableDTO.setLevelCId(purchaseInvoiceTable.getLevelC_id());
                batchWindowTableDTO.setUnitId(purchaseInvoiceTable.getUnit_id());

                batchWindowTableDTO.setBatchNo(purchaseInvoiceTable.getBatch_or_serial());
                batchWindowTableDTO.setB_details_id(purchaseInvoiceTable.getB_details_id());

                if (purchaseInvoiceTable.getManufacturing_date() != null && !purchaseInvoiceTable.getManufacturing_date().isEmpty()) {
                    LocalDate manu_dt = LocalDate.parse(purchaseInvoiceTable.getManufacturing_date());
                    batchWindowTableDTO.setManufacturingDate(manu_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    batchWindowTableDTO.setManufacturingDate("");
                }

                if (purchaseInvoiceTable.getB_expiry() != null && !purchaseInvoiceTable.getB_expiry().isEmpty()) {
                    LocalDate exp_dt = LocalDate.parse(purchaseInvoiceTable.getB_expiry());
                    batchWindowTableDTO.setExpiryDate(exp_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    batchWindowTableDTO.setExpiryDate("");
                }

                batchWindowTableDTO.setMrp(purchaseInvoiceTable.getB_rate());
                batchWindowTableDTO.setPurchaseRate(purchaseInvoiceTable.getB_purchase_rate());

                batchWindowTableDTO.setQuantity(purchaseInvoiceTable.getQuantity());
                batchWindowTableDTO.setFree(purchaseInvoiceTable.getFree());

                batchWindowTableDTO.setDiscountPercentage(purchaseInvoiceTable.getDis_per());
                batchWindowTableDTO.setDiscountAmount(purchaseInvoiceTable.getDis_amt());

                //batchWindowTableDTO.setBarcode(purchaseInvoiceTable.getBarcode());
                batchWindowTableDTO.setMargin(purchaseInvoiceTable.getMin_margin());


                batchWindowTableDTO.setCsr_mh(purchaseInvoiceTable.getRate_c());
                batchWindowTableDTO.setCsr_ai(purchaseInvoiceTable.getRate_d().equals("0.0") ? "" : purchaseInvoiceTable.getRate_d());
                batchWindowTableDTO.setFsr_mh(purchaseInvoiceTable.getRate_a());
                batchWindowTableDTO.setFsr_ai(purchaseInvoiceTable.getRate_b());

                batchWindowTableDTO.setSupplier_id(purchaseInvoiceTable.getLedger_id());
                batchWindowTableDTO.setTax(purchaseInvoiceTable.getTax());

                List<BatchWindowTableDTO> listBatch = new ArrayList<>();
                listBatch.add(batchWindowTableDTO);

                purchaseInvoiceTable.setBatchWindowTableDTOList(listBatch);

                tvPurInvCreateCMPTRow.getItems().addAll(purchaseInvoiceTable);
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(index, tvPurInvCreateCMPTRow, callback);
                PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
//                calculateGst(tableView, callback);

                List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPackingRtn.getAllProductUnitsPackingRtnFlavour(purchaseInvoiceTable.getProduct_id());

                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);

                if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurInvCreateCMPTRow.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }

                } else {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);

                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurInvCreateCMPTRow.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }
                }


                count++;
                index++;
            }
            setAddChargesInTable();
        } else {
            tvPurInvCreateCMPTRow.getItems().addAll(new PurchaseInvoiceTable("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        }

        tblcPurInvCreateCmptSrNo.setCellFactory(new LineNumbersCellFactory());
        tblcPurInvCreateCmptSrNo.setStyle("-fx-alignment: CENTER;");

        tblcPurInvCreateCmptPackage.setCellValueFactory(cellData -> cellData.getValue().packagesProperty());
        tblcPurInvCreateCmptPackage.setStyle("-fx-alignment: CENTER;");
        tblcPurInvCreateCmptLevelA.setCellValueFactory(cellData -> cellData.getValue().levelAProperty());
        tblcPurInvCreateCmptLevelA.setCellFactory(column -> new ComboBoxTableCellForLevelARtn("tblcPurInvCreateCmptLevelA"));


        tblcPurInvCreateCmptLevelB.setCellValueFactory(cellData -> cellData.getValue().levelBProperty());
        tblcPurInvCreateCmptLevelB.setCellFactory(column -> new ComboBoxTableCellForLevelBRtn("tblcPurInvCreateCmptLevelA"));

        tblcPurInvCreateCmptLevelC.setCellValueFactory(cellData -> cellData.getValue().levelCProperty());
        tblcPurInvCreateCmptLevelC.setCellFactory(column -> new ComboBoxTableCellForLevelCRtn("tblcPurInvCreateCmptLevelC"));

        tblcPurInvCreateCmptUnit.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        tblcPurInvCreateCmptUnit.setCellFactory(column -> new ComboBoxTableCellForUnitRtn("tblcPurInvCreateCmptUnit"));

        tblcPurInvCreateCmptParticular.setCellValueFactory(cellData -> cellData.getValue().particularsProperty());
        tblcPurInvCreateCmptParticular.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptParticular", callback, productID_callback, tfPurRtnNarrtion, addPrdCalbak));

        tblcPurInvCreateCmptBatch.setCellValueFactory(cellData -> cellData.getValue().batch_or_serialProperty());
        tblcPurInvCreateCmptBatch.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptBatch", callback, productID_callback, tfPurRtnNarrtion));


        tblcPurInvCreateCmptQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        tblcPurInvCreateCmptQuantity.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptQuantity", callback));

        tblcPurInvCreateCmptRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty());
        tblcPurInvCreateCmptRate.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptRate", callback));

        tblcPurInvCreateCmptGrossAmt.setCellValueFactory(cellData -> cellData.getValue().gross_amountProperty());
        tblcPurInvCreateCmptGrossAmt.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptGrossAmt", callback));

        tblcPurInvCreateCmptDiscPerc.setCellValueFactory(cellData -> cellData.getValue().dis_perProperty());
        tblcPurInvCreateCmptDiscPerc.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptDiscPerc", callback));


        tblcPurInvCreateCmptDiscAmt.setCellValueFactory(cellData -> cellData.getValue().dis_amtProperty());
        tblcPurInvCreateCmptDiscAmt.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptDiscAmt", callback));

        tblcPurInvCreateCmptTaxPerc.setCellValueFactory(cellData -> cellData.getValue().taxProperty());
        tblcPurInvCreateCmptTaxPerc.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptTaxPerc", callback));

        tblcPurInvCreateCmptNetAmt.setCellValueFactory(cellData -> cellData.getValue().net_amountProperty());
        tblcPurInvCreateCmptNetAmt.setCellFactory(column -> new TextFieldTableCellForPurRtn("tblcPurInvCreateCmptNetAmt", callback));
//        tcAction.setCellFactory(column -> new ButtonTableCellDelete(delCallback));

        columnVisibility(tblcPurInvCreateCmptLevelA, Globals.getUserControlsWithSlug("is_level_a"));
        columnVisibility(tblcPurInvCreateCmptLevelB, Globals.getUserControlsWithSlug("is_level_b"));
        columnVisibility(tblcPurInvCreateCmptLevelC, Globals.getUserControlsWithSlug("is_level_c"));

        tblcPurInvCreateCmptLevelA.setText(Globals.getUserControlsNameWithSlug("is_level_a") + "");
        tblcPurInvCreateCmptLevelB.setText(Globals.getUserControlsNameWithSlug("is_level_B") + "");
        tblcPurInvCreateCmptLevelC.setText(Globals.getUserControlsNameWithSlug("is_level_C") + "");
    }

    /**
     * Info : This Method to off the Visibility of the LevelA, LevelB, LevelC Columns.
     */
    private void columnVisibility(TableColumn<PurchaseInvoiceTable, String> column, boolean visible) {
        if (visible) {
//            column.setPrefWidth(USE_COMPUTED_SIZE);
//            column.setMinWidth(USE_PREF_SIZE);
            column.setMaxWidth(Double.MAX_VALUE);
        } else {
            column.prefWidthProperty().unbind();
            column.setPrefWidth(0);
            column.setMinWidth(0);
            column.setMaxWidth(0);
        }
    }


    public void setCbSupplierGSTN(ActionEvent actionEvent) {
        Object[] object = new Object[1];
        object[0] = cmbPurRtnLedgerGst.getSelectionModel().getSelectedItem();
        if (object[0] != null) {
            for (GstDetailsDTO commonDTO : supplierGSTINList) {
                if (object[0].equals(commonDTO)) {
                    supplierGSTIN_ID = commonDTO.getId();
                    gst_no = commonDTO.getGstNo();
                }
            }
        }
    }

    public void purc_disc_per(KeyEvent keyEvent) {
        CommonValidationsUtils.restrictToDecimalNumbers(tfPurRtnDisPer);
        String discText = tfPurRtnDisPer.getText();
        if (!TextUtils.isEmpty(discText)) {
            double totalTaxableAmt = 0.0;
            for (int i = 0; i < tvPurInvCreateCMPTRow.getItems().size(); i++) {
                System.out.println("Gross amt in Prop--->" + tvPurInvCreateCMPTRow.getItems().get(i).getGross_amount());
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(i, tvPurInvCreateCMPTRow, callback);//call row calculation function
                PurchaseInvoiceTable purchaseInvoiceTable = tvPurInvCreateCMPTRow.getItems().get(i);
                totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());

            }
            double disc_per = Double.parseDouble(discText);
            System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
            Double amount = (totalTaxableAmt * disc_per) / 100;
            tfPurRtnDisAmt.setText(String.valueOf(amount));
            PurchaseInvoiceCalculationRtn.discountPropotionalCalculation(disc_per + "", "0", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            }
        } else {
            tfPurRtnDisAmt.setText("");
            /* for (PurchaseInvoiceTable purchaseInvoiceTable : tvPurInvCreateCMPTRow.getItems()) {
                String netAmt = purchaseInvoiceTable.getNet_amount();
                if (!purchaseInvoiceTable.getTaxable_amt().isEmpty()) {
                    Double orgTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                    purchaseInvoiceTable.setNet_amount(netAmt);
                    purchaseInvoiceTable.setTaxable_amt(String.valueOf(orgTaxableAmt));
//                    purchaseInvoiceTable.setTotal_taxable_amt(String.valueOf(orgTaxableAmt));
                }
            }*/

            PurchaseInvoiceCalculationRtn.discountPropotionalCalculation("0", "0", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            }
        }

        //? Calculate Tax for each Row
        PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
    }

    public void purc_disc_amt(KeyEvent keyEvent) {
        CommonValidationsUtils.restrictToDecimalNumbers(tfPurRtnDisAmt);
        String discAmtText = tfPurRtnDisAmt.getText();
        if (!TextUtils.isEmpty(discAmtText)) {
            double totalTaxableAmt = 0.0;
            for (int i = 0; i < tvPurInvCreateCMPTRow.getItems().size(); i++) {
                System.out.println("Gross amt in Prop--->" + tvPurInvCreateCMPTRow.getItems().get(i).getGross_amount());
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(i, tvPurInvCreateCMPTRow, callback);//call row calculation function
                PurchaseInvoiceTable purchaseInvoiceTable = tvPurInvCreateCMPTRow.getItems().get(i);
                totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());

            }
            double disc_amt = Double.parseDouble(discAmtText);
            double percentage = (disc_amt / totalTaxableAmt) * 100;
            tfPurRtnDisPer.setText(String.format("%.2f", percentage));

            PurchaseInvoiceCalculationRtn.discountPropotionalCalculation("0", disc_amt + "", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            }
        } else {
            tfPurRtnDisPer.setText("");
           /* for (PurchaseInvoiceTable purchaseInvoiceTable : tvPurInvCreateCMPTRow.getItems()) {
                String netAmt = purchaseInvoiceTable.getNet_amount();
                if (!purchaseInvoiceTable.getTaxable_amt().isEmpty()) {
                    Double orgTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                    purchaseInvoiceTable.setNet_amount(netAmt);
                    purchaseInvoiceTable.setTaxable_amt(String.valueOf(orgTaxableAmt));
                    purchaseInvoiceTable.setTotal_taxable_amt(String.valueOf(orgTaxableAmt));
                }
            }*/
            PurchaseInvoiceCalculationRtn.discountPropotionalCalculation("0", "0", tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            if (!tfPurchaseRtnAddChgAmt.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculationRtn.additionalChargesCalculation(tfPurchaseRtnAddChgAmt.getText(), tvPurInvCreateCMPTRow, callback);
            }
        }

        //? Calculate Tax for each Row
        PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
    }

    public void onClickAdditionalCharges() {
        Stage stage = (Stage) bpPurRtnRootPane.getScene().getWindow();
        AdditionalCharges.additionalCharges(stage, addChargesDTOList, total_taxable_amt, (input1, input2) -> {
            addChargesDTOList.clear();
            addChargesDTOList.addAll(input2);
            tfPurchaseRtnAddChgAmt.setText(input1[1]);
            PurchaseInvoiceCalculationRtn.additionalChargesCalculation(input1[1], tvPurInvCreateCMPTRow, callback);

        });
    }


    // Scene Initialization
    public void sceneInitilization() {
        bpPurRtnRootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof Stage) {
                Communicator.stage = (Stage) newScene.getWindow();
            }
        });
    }

    // Auto Setting the PurChase Rtn Sr. No.
    private void getPurRtnSrNumber() {
        APIClient apiClient = null;
        try {
            purRtnLogger.debug("Get getPurRtnSrNumber Started...");
            apiClient = new APIClient(EndPoints.GET_PUR_RTN_LAST_RECORD, "", RequestType.GET);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        tfPurRtnsr.setText("" + jsonObject.get("count").getAsString());
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in getPurRtnSrNumber()" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in getPurRtnSrNumber()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            purRtnLogger.debug("Get getPurRtnSrNumber End...");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purRtnLogger.error("Exception in getVoucherNumber():" + exceptionAsString);
        }

    }

    // Get  All Purchase Accounts
    public void getPurchaseAccounts() {
        APIClient apiClient = null;
        try {
            purRtnLogger.debug("Get getPurchaseAccounts Started...");
            apiClient = new APIClient(EndPoints.getPurChallPurchaseAccounts, "", RequestType.GET);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<CommonDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseObject = jsonObject.getAsJsonArray("list");


                        if (responseObject.size() > 0) {
                            for (JsonElement element : responseObject) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id").getAsString();
                                String name = item.get("name").getAsString();
                                String unique_code = item.get("unique_code").getAsString();


                                observableList.add(new CommonDTO(name, id));
                                cmbPurRtnPurAC.setItems(observableList);
                                cmbPurRtnPurAC.getSelectionModel().selectFirst();
                                cmbPurRtnPurAC.setConverter(new StringConverter<CommonDTO>() {
                                    @Override
                                    public String toString(CommonDTO o) {
                                        return o.getText();
                                    }

                                    @Override
                                    public CommonDTO fromString(String s) {
                                        return null;
                                    }
                                });

                            }

                        } else {
                            purRtnLogger.debug("Get getPurchaseAccounts responseObject is null...");
                            System.out.println("responseObject is null");
                        }
                    } else {
                        purRtnLogger.debug("Get getPurchaseAccounts Error in response...");
                        System.out.println("Error in response");
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in getPurchaseAccounts()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in getPurchaseAccounts()" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.start();
            purRtnLogger.debug("Get getPurchaseAccounts  end");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    // to Upload Image
   /* @FXML
    private void handleChooseFile() {

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter excelFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx, *.xls)", "*.xlsx", "*.xls");
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json");
        fileChooser.getExtensionFilters().addAll(textFilter, pdfFilter, excelFilter, jsonFilter);
        Stage stage = (Stage) btnPurChallChooseFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            tfPurChallChooseFileText.setText(selectedFile.getName());
        }
    }*/

    // GstDetails DTO to Set GST
    public GstDetailsDTO convertJsonToGstDetailsDTO(JsonElement jsonElement) {
        // Parse the JsonElement and create a GstDetailsDTO object
        // For example:
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String gstNumber = jsonObject.get("gstNumber").getAsString();
        String id = jsonObject.get("id").getAsString();
        String state = jsonObject.get("state").getAsString();
        // Parse other properties as needed

        return new GstDetailsDTO(id, gstNumber, state); // Assuming GstDetailsDTO constructor accepts gstNumber
    }

    // Helper method to round the value to a specified decimal places String.format("%.3f"; costVal)
    private double roundDigit(double value, int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(value * factor) / factor;
    }

    //     Calling Create API of Purchase Return Start
    public void createPurchaseReturnNew() {

        Object idValue = invoice_data_map.get("id");
        if (idValue != null) {
            purchase_return_map.put("id", idValue.toString());
            purchase_return_map.put("rowDelDetailsIds", deletedRows.toString());
        }

        purchase_return_map.put("taxFlag", String.valueOf(taxFlag));

        purchase_return_map.put("purchase_return_date", Communicator.text_to_date.fromString(tfPurRtnInvDate.getText()).toString());

        purchase_return_map.put("newReference", "false");
        purchase_return_map.put("paymentMode", selectedPayMode);
        purchase_return_map.put("pur_return_invoice_no", tfPurRtnInvNum.getText());
        purchase_return_map.put("source", "pur_invoice");//
        purchase_return_map.put("pur_invoice_id", invoiceId);//

        purchase_return_map.put("purchase_account_id", cmbPurRtnPurAC.getValue().getId());

        purchase_return_map.put("purchase_return_sr_no", tfPurRtnsr.getText());

        purchase_return_map.put("narration", tfPurRtnNarrtion.getText());

        purchase_return_map.put("supplier_code_id", ledgerId);

        purchase_return_map.put("isRoundOffCheck", String.valueOf(chbPurRtnRoundOff.isSelected()));

        purchase_return_map.put("roundoff", lblPurRtnRoundOff.getText());

        purchase_return_map.put("totalamt", lblPurRtnTotalBillAmount.getText());

        purchase_return_map.put("total_purchase_discount_amt", lblPurRtnTotalDiscount.getText());

        purchase_return_map.put("gstNo", gst_no);

        purchase_return_map.put("tcs_per", "0.00");

        purchase_return_map.put("tcs_amt", "0.00");

        purchase_return_map.put("tcs_mode", "");

        purchase_return_map.put("purchase_discount", tfPurRtnDisPer.getText().isEmpty() || tfPurRtnDisPer == null ? "0.0" : tfPurRtnDisPer.getText());

        purchase_return_map.put("purchase_discount_amt", tfPurRtnDisAmt.getText().isEmpty() || tfPurRtnDisAmt == null ? "0.0" : tfPurRtnDisAmt.getText());

        purchase_return_map.put("additionalChargesTotal", tfPurchaseRtnAddChgAmt.getText().isEmpty() ? "0" : tfPurchaseRtnAddChgAmt.getText());
        purchase_return_map.put("total_tax_amt", lblPurRtnTotalTax.getText());
        purchase_return_map.put("total_base_amt", lblPurRtnGrossTotal.getText());
        purchase_return_map.put("additionalCharges", addChargesDTOList.toString());
        List<PurchaseInvoiceTable> currentItems = new ArrayList<>(tvPurInvCreateCMPTRow.getItems());

        if (!currentItems.isEmpty()) {
            PurchaseInvoiceTable lastItem = currentItems.get(currentItems.size() - 1);

            if (lastItem.getParticulars() != null && lastItem.getParticulars().isEmpty()) {
                currentItems.remove(currentItems.size() - 1);
            }
        }
        List<PurchaseInvoiceTable> list = new ArrayList<>(currentItems);
        for (PurchaseInvoiceTable purchaseInvoiceTable : list) {
            if (!purchaseInvoiceTable.getTotal_taxable_amt().equals("0.0")) {
                purchaseInvoiceTable.setTotal_amt(purchaseInvoiceTable.getTotal_taxable_amt());
            } else {
                purchaseInvoiceTable.setTotal_amt(purchaseInvoiceTable.getTaxable_amt());
            }
        }

        Double total_row_gross_amt = 0.0;

        for (PurchaseInvoiceTable purchaseInvoiceTable : list) {
            total_row_gross_amt = total_row_gross_amt + Double.parseDouble(
                    purchaseInvoiceTable.getGross_amount1().isEmpty() ? "0.0" : purchaseInvoiceTable.getGross_amount1());
        }

        purchase_return_map.put("total_row_gross_amt", String.valueOf(total_row_gross_amt));

        purchase_return_map.put("row", list.toString());

        System.out.println(purchase_return_map);
        APIClient apiClient = null;
        try {
            String formData = Globals.mapToStringforFormData(purchase_return_map);

            if (purRtnEditId > 0) {
                apiClient = new APIClient(EndPoints.EDIT_PUR_RETURN_ENDPOINT, formData, RequestType.FORM_DATA);
                //? HIGHLIGHT
                PurchaseReturnListController.editedPurchaseRtnId = idValue.toString(); //? Set the ID for editing
            } else {
                apiClient = new APIClient(EndPoints.CREATE_PUR_RETURN_ENDPOINT, formData, RequestType.FORM_DATA);
                //? HIGHLIGHT
                PurchaseReturnListController.isNewPurchaseChallanCreated = true; //? Set the flag for new creation
            }

            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println("jsonObject >> : " + jsonObject);
                    message = jsonObject.get("message").getAsString();
                    int status = jsonObject.get("responseStatus").getAsInt();
                    if (status == 200) {
                        SingleInputDialogs.productId = "";
                        SingleInputDialogs.ledId = "";
                        //Successfully Alert Popup After Edit and Redirect to List Page
                        AlertUtility.AlertSuccessTimeout(AlertUtility.alertTypeSuccess, message, input -> {
                            GlobalController.getInstance().addTabStatic(PURCHASE_RETURN_LIST_SLUG, false);
                        });
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
//                    creditNoteLogger.error("Network API cancelled in createCreditNote()" + workerStateEvent.getSource().getValue().toString());

                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
//                    creditNoteLogger.error("Network API failed in createCreditNote()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
        }

    }


    private void getPurRtnById(Integer purRtnEditId) {
        try {

            System.out.println("GETBYID=>" + purRtnEditId);
            Map<String, String> body = new HashMap<>();
            body.put("id", purRtnEditId.toString());
            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_BY_ID_PUR_RTN_ENDPOINT);
            PurReturnEditDTO responseBody = new Gson().fromJson(response.body(), PurReturnEditDTO.class);
            if (responseBody.getResponseStatus() == 200) {
                setPurRtnFormData(responseBody);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purRtnLogger.error("Exception in getPurRtnById():" + exceptionAsString);
        }
    }


    // Setting Data to the fields
    private void setPurRtnFormData(PurReturnEditDTO responseBody) {
        try {
            String responseBdy = new Gson().toJson(responseBody);
            System.out.println("ResBdy=>" + responseBdy);
            if (purchaseOrderDTO == null) {
                btnPurRtnFinalSubmit.setText("Update");
               // tfPurRtnLedgName.setText();
            } else {
                btnPurRtnFinalSubmit.setText("Submit");
            }
            tfPurRtnInvNum.setText(responseBody.getInvoiceData().getInvoiceNo());
            tfPurRtnNarrtion.setText(responseBody.getNarration());
            lblPurRtnTotalQty.setText(String.valueOf(responseBody.getTotalQty()));
            lblPurRtnFreeQty.setText(String.valueOf(responseBody.getTotalFreeQty()));
            lblPurRtnRoundOff.setText(String.valueOf(responseBody.getInvoiceData().getRoundoff()));
            chbPurRtnRoundOff.setSelected(responseBody.getInvoiceData().getRoundOffCheck());
            ledgerId = String.valueOf(responseBody.getInvoiceData().getSupplierId());
            SingleInputDialogs.ledId = ledgerId;
            tranxLedgerDetailsFun(ledgerId);
            edit_response_map.put("tcs_mode", responseBody.getTcsMode());
            edit_response_map.put("narration", responseBody.getNarration());
            edit_response_map.put("discountLedgerId", responseBody.getDiscountLedgerId());
            edit_response_map.put("discountInAmt", responseBody.getDiscountInAmt());
            edit_response_map.put("discountInPer", responseBody.getDiscountInPer());
            edit_response_map.put("totalPurchaseDiscountAmt", responseBody.getTotalPurchaseDiscountAmt());
            edit_response_map.put("totalQty", responseBody.getTotalQty());
            edit_response_map.put("totalFreeQty", responseBody.getTotalFreeQty());
            edit_response_map.put("grossTotal", responseBody.getGrossTotal());
            edit_response_map.put("totalTax", responseBody.getTotalTax());
            edit_response_map.put("additionLedger1", responseBody.getAdditionLedger1());
            edit_response_map.put("additionLedgerAmt1", responseBody.getAdditionLedgerAmt1());
            edit_response_map.put("additionLedger2", responseBody.getAdditionLedger2());
            edit_response_map.put("additionLedgerAmt2", responseBody.getAdditionLedgerAmt2());
            edit_response_map.put("additionLedger3", responseBody.getAdditionLedger3());
            edit_response_map.put("additionLedgerAmt3", responseBody.getAdditionLedgerAmt3());
            edit_response_map.put("invoiceData", responseBody.getInvoiceData());
            edit_response_map.put("barcodeList", responseBody.getBarcodeList());
            edit_response_map.put("additionalCharges", responseBody.getAdditionalCharges());
            edit_response_map.put("paymentMode", responseBody.getPaymentMode());


            rowListDTOS.addAll(responseBody.getRow());
            addChargesResLst.addAll(responseBody.getAdditionalCharges());

            InvoiceData invoiceDataResDTO = responseBody.getInvoiceData();
            invoice_data_map.put("id", String.valueOf(invoiceDataResDTO.getId()));
            invoice_data_map.put("invoice_dt", String.valueOf(invoiceDataResDTO.getInvoiceDt()));
            invoice_data_map.put("invoice_no", String.valueOf(invoiceDataResDTO.getInvoiceNo()));
            invoice_data_map.put("tranx_unique_code", String.valueOf(invoiceDataResDTO.getTranxUniqueCode()));
            invoice_data_map.put("purchase_sr_no", String.valueOf(invoiceDataResDTO.getPurchaseSrNo()));
            invoice_data_map.put("purchase_account_ledger_id", String.valueOf(invoiceDataResDTO.getPurchaseAccountLedgerId()));
            invoice_data_map.put("supplierId", String.valueOf(invoiceDataResDTO.getSupplierId()));
            //ledgerId = invoice_data_map.get("supplierId");
            System.out.println("Tranx Date:" + invoiceDataResDTO.getTransactionDt());
            invoice_data_map.put("transaction_dt", String.valueOf(invoiceDataResDTO.getTransactionDt()));
            invoice_data_map.put("additional_charges_total", String.valueOf(invoiceDataResDTO.getAdditionalChargesTotal()));
            invoice_data_map.put("gstNo", String.valueOf(invoiceDataResDTO.getGstNo()));
            invoice_data_map.put("isRoundOffCheck", String.valueOf(invoiceDataResDTO.getRoundOffCheck()));
            invoice_data_map.put("roundoff", String.valueOf(invoiceDataResDTO.getRoundoff()));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purRtnLogger.error("Exception in setPurRtnFormData():" + exceptionAsString);
        }
    }

    public String ledgersById(Long id) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(id));

            String formData = mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.getLedgersById);

            String json = response.body();
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            JsonObject responseObj = jsonObject.getAsJsonObject("response");
            String ledgerName = responseObj.get("ledger_name").getAsString();
            ledgerStateCode = responseObj.get("ledgerStateCode").getAsString();
            JsonArray gstdetailsArray = responseObj.getAsJsonArray("gstdetails");
            if (gstdetailsArray != null) {
                for (JsonElement element : gstdetailsArray) {
                    JsonObject gstdetail = element.getAsJsonObject();
                    String gstin = gstdetail.get("gstin").getAsString();
                    String gst_id = gstdetail.get("id").getAsString();
                    supplierGSTINList.add(new GstDetailsDTO(gst_id, gstin, ledgerStateCode));
                }
            }
            if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
                taxFlag = true;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            purRtnLogger.error("Exception in ledgersById():" + exceptionAsString);
        }
        return ledgerName;
    }

    private void setAddChargesInTable() {
        Double addChgAmt = 0.0;
        Double totalAddChgAmt = 0.0;
        for (AdditionalCharge addChgLst : addChargesResLst) {
            CustomAddChargesDTO customAddChargesDTO = new CustomAddChargesDTO();
            customAddChargesDTO.setAdditional_charges_details_id(String.valueOf(addChgLst.getAdditionalChargesDetailsId()));
            customAddChargesDTO.setLedgerId(String.valueOf(addChgLst.getLedgerId()));
            customAddChargesDTO.setAmt(String.valueOf(addChgLst.getAmt()));
            customAddChargesDTO.setPercent(String.valueOf(addChgLst.getPercent()));
            addChargesDTOList.add(customAddChargesDTO);

            addChgAmt = addChgLst.getAmt();
            totalAddChgAmt += addChgAmt;
        }
        tfPurchaseRtnAddChgAmt.setText(totalAddChgAmt.toString());
        PurchaseInvoiceCalculationRtn.additionalChargesCalculation(totalAddChgAmt.toString(), tvPurInvCreateCMPTRow, callback);

    }


    /**
     * Info :   For showing the details of product after selection of product
     *
     * @Author : Vinit Chilaka
     */
    private void tranxProductDetailsFun(String id) {
        //todo: activating the product tab
        tranxPurRtnTabPane.getSelectionModel().select(tabPurRtnProduct);

        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("product_id", id);
            String formData = Globals.mapToStringforFormData(map);
//        System.out.println("FormData: " + formData);
            apiClient = new APIClient(EndPoints.TRANSACTION_PRODUCT_DETAILS, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                    System.out.println("jsonObject Res ==>" + jsonObject);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonObject productJsonObj = jsonObject.getAsJsonObject("result");

                        txtPurRtnProductBrand.setText(productJsonObj.get("brand").getAsString());
                        txtPurRtnProductGroup.setText(productJsonObj.get("group").getAsString());
                        txtPurRtnProductSubGroup.setText(productJsonObj.get("subgroup").getAsString());
                        txtPurRtnProductCategory.setText(productJsonObj.get("category").getAsString());
                        txtPurRtnProductHsn.setText(productJsonObj.get("hsn").getAsString());
                        txtPurRtnProductTaxType.setText(productJsonObj.get("tax_type").getAsString());
                        txtPurRtnProductTaxPer.setText(String.valueOf(productJsonObj.get("tax_per").getAsInt()));
                        txtPurRtnProductMarginPer.setText(productJsonObj.get("margin_per").getAsString());
                        txtPurRtnProductCost.setText(productJsonObj.get("cost").getAsString());
                        txtPurRtnProductShelfId.setText(productJsonObj.get("shelf_id").getAsString());
                        txtPurRtnProductMinStock.setText(String.valueOf(productJsonObj.get("min_stocks").getAsInt()));
                        txtPurRtnProductMaxStock.setText(String.valueOf(productJsonObj.get("max_stocks").getAsInt()));
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }


    }

    /**
     * Info :  For showing the details of ledger after selection of ledger.
     *
     * @Author : Vinit Chilaka
     */
    private void tranxLedgerDetailsFun(String id) {
        //todo: activating the product tab
        tranxPurRtnTabPane.getSelectionModel().select(tabPurRtnLedger);

        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("ledger_id", id);
            String formData = Globals.mapToStringforFormData(map);
//        System.out.println("FormData: " + formData);
            apiClient = new APIClient(EndPoints.TRANSACTION_LEDGER_DETAILS, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                    System.out.println("jsonObject Res ==>" + jsonObject);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonObject jsonObj = jsonObject.getAsJsonObject("result");
//                        System.out.println("jsonObj " + jsonObj);
                        CompanyStateCode = jsonObject.get("company_state_code").getAsLong();
                        stateCode = jsonObj.get("stateCode").getAsLong();
                        // setting values
                        txtPurRtnGstNo.setText(jsonObj.get("gst_number").getAsString());
                        txtPurRtnArea.setText(jsonObj.get("area").getAsString());
                        txtPurRtnBank.setText(jsonObj.get("bank_name").getAsString());
                        txtPurRtnContactPerson.setText(jsonObj.get("contact_name").getAsString());
                        txtPurRtnCreditDays.setText(jsonObj.get("credit_days").getAsString());
                        txtPurRtnFssai.setText(jsonObj.get("fssai_number").getAsString());
                        txtPurRtnLicenseNo.setText(jsonObj.get("license_number").getAsString());
                        txtPurRtnRoute.setText(jsonObj.get("route").getAsString());
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in tranxLedgerDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in tranxLedgerDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }


    }

    /**
     * Info :  Set the SUpplier Details of the Selected Product Bottom in Supplier Table
     *
     * @Author : Vinit Chilaka
     */
    private void getSupplierListbyProductId(String id) {
        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("productId", id);
            String formData = Globals.mapToStringforFormData(map);
            apiClient = new APIClient(EndPoints.GET_SUPPLIERLIST_BY_PRODUCTID_ENDPOINT, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray dataArray = jsonObject.getAsJsonArray("data");
                        System.out.println("dataArray >> : " + dataArray);
                        //todo: getting values
                        ObservableList<SalesOrderSupplierDetailsDTO> supplierDataList = FXCollections.observableArrayList();
                        for (JsonElement element : dataArray) {
                            JsonObject item = element.getAsJsonObject();
                            String supplierName = item.get("supplier_name").getAsString();
                            String supplierInvNo = item.get("invoice_no").getAsString();
                            String supplierInvDate = item.get("invoice_date").getAsString();
                            String supplierBatch = item.get("batch").getAsString();
                            String supplierMrp = item.get("mrp").getAsString();
                            String supplierQty = item.get("rate").getAsString();
                            String supplierRate = item.get("quantity").getAsString();
                            String supplierCost = item.get("cost").getAsString();
                            String supplierDisPer = item.get("dis_per").getAsString();
                            String supplierDisAmt = item.get("dis_amt").getAsString();

                            supplierDataList.add(new SalesOrderSupplierDetailsDTO(supplierName, supplierInvNo, supplierInvDate, supplierBatch, supplierMrp, supplierRate, supplierQty, supplierCost, supplierDisPer, supplierDisAmt));
                        }
                        tcPCRtnPrdHistSupplierName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
                        tcPCRtnPrdHistInvNo.setCellValueFactory(new PropertyValueFactory<>("supplierInvNo"));
                        tcPCRtnPrdHistInvDate.setCellValueFactory(new PropertyValueFactory<>("supplierInvDate"));
                        tcPCRtnPrdHistBatch.setCellValueFactory(new PropertyValueFactory<>("supplierBatch"));
                        tcPCRtnPrdHistMRP.setCellValueFactory(new PropertyValueFactory<>("supplierMrp"));
                        tcPCRtnPrdHistQty.setCellValueFactory(new PropertyValueFactory<>("supplierQty"));
                        tcPCRtnPrdHistRate.setCellValueFactory(new PropertyValueFactory<>("supplierRate"));
                        tcPCRtnPrdHistCost.setCellValueFactory(new PropertyValueFactory<>("supplierCost"));
                        tcPCRtnPrdHistDisPer.setCellValueFactory(new PropertyValueFactory<>("supplierDisPer"));
                        tcPCRtnPrdHistDisAmt.setCellValueFactory(new PropertyValueFactory<>("supplierDisAmt"));

                        tvPurRtnSupplierDetails.setItems(supplierDataList);

                    }


                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }
    }

    /**
     * Info :  For showing the details of Batch after Creating the Batch.
     *
     * @Author : Vinit Chilaka
     */
    private void tranxBatchDetailsFun(String bNo, String bId) {
        APIClient apiClient = null;
        tranxPurRtnTabPane.getSelectionModel().select(tabPurRtnBatch);
        purRtnLogger.debug("Get Supplier List Data Started...");
        System.out.println("In Batch FUN >> " + bNo + " >> " + bId);
        Map<String, String> map = new HashMap<>();
        map.put("batchNo", bNo);
        map.put("id", bId);
//        requestData.append("batchNo", batchNo.batch_no);
//        requestData.append("id", batchNo.b_details_id);
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("FormData: " + formData);
//        HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_SUPPLIERLIST_BY_PRODUCTID_ENDPOINT);
        apiClient = new APIClient(EndPoints.GET_BATCH_DETAILS_ENDPOINT, formData, RequestType.FORM_DATA);

        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                System.out.println("jsonObject =>" + jsonObject);
                if (jsonObject.get("responseStatus").getAsInt() == 200) {
                    JsonObject batchJsonObj = jsonObject.getAsJsonObject("response");

                    txtPurRtnBatchBatch.setText(batchJsonObj.get("batchNo").getAsString());
                    txtPurRtnBatchMRP.setText(batchJsonObj.get("batchMrp").getAsString());
                    txtPurRtnBatchHMfgDt.setText(DateConvertUtil.convertDispDateFormat(batchJsonObj.get("mfgDate").getAsString()));
                    txtPurRtnBatchExpDt.setText(DateConvertUtil.convertDispDateFormat(batchJsonObj.get("expDate").getAsString()));
                    txtPurRtnBatchPurRate.setText(batchJsonObj.get("pur_rate").getAsString());
                    txtPurRtnBatchAvlStk.setText(batchJsonObj.get("qty").getAsString());
                    txtPurRtnBatchDisPer.setText(batchJsonObj.get("b_dis_per").getAsString());
                    txtPurRtnBatchDisAmt.setText(batchJsonObj.get("b_dis_amt").getAsString());
                    txtPurRtnBatchSaleRate.setText(batchJsonObj.get("sales_rate").getAsString());
                    txtPurRtnBatchFSRMH.setText(batchJsonObj.get("fsrmh").getAsString());
                    txtPurRtnBatchFSRAI.setText(batchJsonObj.get("fsrai").getAsString());
                    txtPurRtnBatchCSRMH.setText(batchJsonObj.get("csrmh").getAsString());
                    txtPurRtnBatchCSRAI.setText(batchJsonObj.get("csrai").getAsString());
                    txtPurRtnBatchCSRAI.setText(batchJsonObj.get("csrai").getAsString());
                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                purRtnLogger.error("Network API cancelled in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                purRtnLogger.error("Network API failed in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        purRtnLogger.debug("Get Supplier List Data End...");

    }

    public void validatePurchaseReturn() {
        APIClient apiClient = null;
        try {

            Map<String, String> map = new HashMap<>();
            map.put("supplier_id", ledgerId);
            map.put("bill_no", String.valueOf(tfPurRtnInvNum.getText()));
            String formData = Globals.mapToStringforFormData(map);
            System.out.println("for,madata >>>  " + formData);
            apiClient = new APIClient(EndPoints.VALIDATE_PUR_RETURN, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    System.out.println(" jsonObject : >> " + jsonObject);
                    if (jsonObject.get("responseStatus").getAsInt() != 200) {
                        String message = jsonObject.get("message").getAsString();
                        AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeError, message, in -> {
                            tfPurRtnInvNum.requestFocus();
                        });
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in functionCompanyDuplicate()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in functionCompanyDuplicate()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO : use logger for alert messages
//            showAlert("Falied to connect server! ");
            AlertUtility.AlertDialogForError("WARNING", "Failed to Connect Server !", input -> {
                if (input) {
                }
            });
        } finally {
            apiClient = null;
        }
    }

    public void validatePurchaseReturnUpdate() {
        APIClient apiClient = null;
        try {

            Map<String, String> map = new HashMap<>();
            map.put("return_id", String.valueOf(purRtnEditId));
            map.put("supplier_id", ledgerId);
            map.put("bill_no", String.valueOf(tfPurRtnInvNum.getText()));
            String formData = Globals.mapToStringforFormData(map);
            apiClient = new APIClient(EndPoints.VALIDATE_PUR_RETURN_UPDATE, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() != 200) {
                        String message = jsonObject.get("message").getAsString();
                        AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeError, message, in -> {
                            tfPurRtnInvNum.requestFocus();
                        });
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API cancelled in functionCompanyDuplicate()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    purRtnLogger.error("Network API failed in functionCompanyDuplicate()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO : use logger for alert messages
//            showAlert("Falied to connect server! ");
            AlertUtility.AlertDialogForError("WARNING", "Failed to Connect Server !", input -> {
                if (input) {
                }
            });
        } finally {
            apiClient = null;
        }
    }

    private void tfReturnNoValidation() {
        tfPurRtnInvNum.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfPurRtnInvNum.getText().isEmpty()) {
                    tfPurRtnInvNum.requestFocus();
                } else {
                    String btText = btnPurRtnFinalSubmit.getText();
                    if (btText.equalsIgnoreCase("Submit")) {
                        validatePurchaseReturn();
                    } else {
                        validatePurchaseReturnUpdate();
                    }
                }

            }
        });
    }

    public void setPurChallDataToProduct() {

        String tranxDate = Communicator.text_to_date.fromString(tfPurInvCreateTranxDate.getText()).toString();
        String ledgrId = ledgerId;
        String gstNum = cmbPurRtnLedgerGst.getId();
        String purSerNum = tfPurRtnsr.getText();
        String challanNo = tfPurRtnInvNum.getText();
        String purAcc = cmbPurRtnPurAC.getId();
        String challanDate = Communicator.text_to_date.fromString(tfPurRtnInvDate.getText()).toString();

        PurTranxToProductRedirectionDTO mData = new PurTranxToProductRedirectionDTO();
        mData.setRedirect(FxmFileConstants.PURCHASE_RETURN_CREATE_SLUG);
        mData.setTranxDate(tranxDate);
        mData.setLedegrId(ledgrId);
        mData.setGstNum(gstNum);
        mData.setPurSerNum(purSerNum);
        mData.setChallanNo(challanNo);
        mData.setPurAcc(purAcc);
        mData.setChallanDate(challanDate);
        mData.setLedgerRed(isLedgerRed);
        mData.setProductRed(isProductRed);
        mData.setTranxType("purchase Return");
        mData.setPurChallEditId(purRtnEditId);
        mData.setRediPrdCurrIndex(rediCurrIndex);
        List<PurchaseInvoiceTable> currentItems = new ArrayList<>(tvPurInvCreateCMPTRow.getItems());
        List<PurchaseInvoiceTable> list = new ArrayList<>(currentItems);

        mData.setRowData(list);

//        for (PurchaseInvoiceTable rowData : tvPurInvCreateCMPTRow.getItems()) {
//            PurchaseInvoiceTable mData1 = new PurchaseInvoiceTable();
//            mData1.setProduct_id(rowData.getProduct_id());
////            mData1.set
//        }
        if (isProductRed == true) {
            GlobalController.getInstance().addTabStaticWithIsNewTab("product-create", false, mData);
        }
        if (isLedgerRed == true) {
            GlobalController.getInstance().addTabStaticWithIsNewTab("ledger-create", false, mData);
        }


    }


    public void setRedirectData(Object inObj) {
        PurTranxToProductRedirectionDTO franchiseDTO = (PurTranxToProductRedirectionDTO) inObj;
        Integer redirectIndex = franchiseDTO.getRediPrdCurrIndex();
        String tranxDate = Communicator.text_to_date.fromString(tfPurInvCreateTranxDate.getText()).toString();
        String ledgrId = ledgerId;
        String gstNum = cmbPurRtnLedgerGst.getId();
        String purSerNum = tfPurRtnsr.getText();
        String challanNo = tfPurRtnInvNum.getText();
        String purAcc = cmbPurRtnPurAC.getId();
        String challanDate = Communicator.text_to_date.fromString(tfPurRtnInvDate.getText()).toString();

        tfPurRtnInvNum.setText(franchiseDTO.getChallanNo());
        ledgerId = franchiseDTO.getLedegrId();
        tfPurRtnsr.setText(franchiseDTO.getPurSerNum());
        tfPurRtnLedgName.setText(ledgersById(Long.valueOf(ledgerId)));
        int cnt = 0, index = 0;
        tvPurInvCreateCMPTRow.getItems().clear();
        purRtnEditId = franchiseDTO.getPurChallEditId();
        PurInvoiceCommunicator.levelAForPurInvoiceObservableList.clear();
        if (franchiseDTO.getProductRed() == true) {
            Long productId = Long.valueOf(franchiseDTO.getRedirectProductId());
            int rindex = franchiseDTO.getRediPrdCurrIndex();
            JsonObject obj = productRedirectById(productId);

            for (PurchaseInvoiceTable purchaseInvoiceTable : franchiseDTO.getRowData()) {
                if (index == rindex) {
                    purchaseInvoiceTable.setProduct_id(productId + "");
                    purchaseInvoiceTable.setParticulars(obj.get("productName").getAsString());
                    purchaseInvoiceTable.setPackages(obj.get("package").getAsString());
                    purchaseInvoiceTable.setIs_batch(obj.get("is_batch").getAsString());
                    purchaseInvoiceTable.setRate_a(obj.get("fsrmh").getAsString());
                    purchaseInvoiceTable.setRate_b(obj.get("fsrai").getAsString());
                    purchaseInvoiceTable.setRate_c(obj.get("csrmh").getAsString());
                    purchaseInvoiceTable.setRate_d(obj.get("csrai").getAsString());
                    purchaseInvoiceTable.setTax(obj.get("tax_per").getAsString());
                    purchaseInvoiceTable.setUnit_id(obj.get("unit_id").getAsString());
                    purchaseInvoiceTable.setB_rate(obj.get("mrp").getAsString());
                    purchaseInvoiceTable.setB_purchase_rate(obj.get("purchaseRate").getAsString());
                    purchaseInvoiceTable.setCurrent_index(String.valueOf(index));
                    BatchWindowTableDTO batchWindowTableDTO = new BatchWindowTableDTO();

                    batchWindowTableDTO.setProductId(purchaseInvoiceTable.getProduct_id());
                    batchWindowTableDTO.setCsr_mh(purchaseInvoiceTable.getRate_c());
                    batchWindowTableDTO.setCsr_ai(purchaseInvoiceTable.getRate_d().equals("0.0") ? "" : purchaseInvoiceTable.getRate_d());
                    batchWindowTableDTO.setFsr_mh(purchaseInvoiceTable.getRate_a());
                    batchWindowTableDTO.setFsr_ai(purchaseInvoiceTable.getRate_b());
                    batchWindowTableDTO.setMrp(purchaseInvoiceTable.getB_rate());
                    System.out.println("iceTable.getLevelA_id()" + purchaseInvoiceTable.getLevelA_id());

                    batchWindowTableDTO.setUnitId(purchaseInvoiceTable.getUnit_id());
                    batchWindowTableDTO.setBatchNo(purchaseInvoiceTable.getBatch_or_serial());
                    batchWindowTableDTO.setB_details_id("");
                    if (purchaseInvoiceTable.getManufacturing_date() != null && !purchaseInvoiceTable.getManufacturing_date().isEmpty()) {
                        LocalDate manu_dt = LocalDate.parse(purchaseInvoiceTable.getManufacturing_date());
                        batchWindowTableDTO.setManufacturingDate(manu_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } else {
                        batchWindowTableDTO.setManufacturingDate("");
                    }

                    if (purchaseInvoiceTable.getB_expiry() != null && !purchaseInvoiceTable.getB_expiry().isEmpty()) {
                        LocalDate exp_dt = LocalDate.parse(purchaseInvoiceTable.getB_expiry());
                        batchWindowTableDTO.setExpiryDate(exp_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } else {
                        batchWindowTableDTO.setExpiryDate("");
                    }

                    batchWindowTableDTO.setMrp(purchaseInvoiceTable.getB_rate());
                    batchWindowTableDTO.setPurchaseRate(purchaseInvoiceTable.getB_purchase_rate());

                    batchWindowTableDTO.setQuantity(purchaseInvoiceTable.getQuantity());
                    batchWindowTableDTO.setFree(purchaseInvoiceTable.getFree());

                    batchWindowTableDTO.setDiscountPercentage(purchaseInvoiceTable.getDis_per());
                    batchWindowTableDTO.setDiscountAmount(purchaseInvoiceTable.getDis_amt());

                    //batchWindowTableDTO.setBarcode(purchaseInvoiceTable.getBarcode());
                    batchWindowTableDTO.setMargin(purchaseInvoiceTable.getMin_margin());


                    batchWindowTableDTO.setCsr_mh(purchaseInvoiceTable.getRate_c());
                    batchWindowTableDTO.setCsr_ai(purchaseInvoiceTable.getRate_d().equals("0.0") ? "" : purchaseInvoiceTable.getRate_d());
                    batchWindowTableDTO.setFsr_mh(purchaseInvoiceTable.getRate_a());
                    batchWindowTableDTO.setFsr_ai(purchaseInvoiceTable.getRate_b());

                    batchWindowTableDTO.setSupplier_id(purchaseInvoiceTable.getLedger_id());
                    batchWindowTableDTO.setTax(purchaseInvoiceTable.getTax());
                    batchWindowTableDTO.setPurchaseRate(purchaseInvoiceTable.getB_purchase_rate());
                    List<BatchWindowTableDTO> listBatch = new ArrayList<>();
                    listBatch.add(batchWindowTableDTO);

                    purchaseInvoiceTable.setBatchWindowTableDTOList(listBatch);


                }

                tvPurInvCreateCMPTRow.getItems().addAll(purchaseInvoiceTable);
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(index, tvPurInvCreateCMPTRow, callback);
                PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
//                calculateGst(tableView, callback);

                List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPackingRtn.getAllProductUnitsPackingRtnFlavour(purchaseInvoiceTable.getProduct_id());
                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
                if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                    tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurInvCreateCMPTRow.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(null);
                            tvPurInvCreateCMPTRow.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }

                } else {
                    System.out.println("Index else >> : " + index);
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
                }

                cnt++;
                index++;
            }
        }

        int inx = 0;
        System.out.println("is ledgeriiiiiiiiiii" + franchiseDTO.getLedgerRed());
        // tvPurInvCreateCMPTRow.getItems().clear();
        if (franchiseDTO.getLedgerRed() == true) {
            System.out.println("is ledger" + franchiseDTO.getLedgerRed());
            for (PurchaseInvoiceTable rowData : franchiseDTO.getRowData()) {


                System.out.println("String.valueOf(rowData.getRowData().get(0))" + rowData.getProduct_id());
//                mData1.setProduct_id(rowData.getProduct_id());
//                mData1.setParticulars(rowData.getParticulars());
//                mData1.setPackages(rowData.getPackages());
                tvPurInvCreateCMPTRow.getItems().add(rowData);
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(index, tvPurInvCreateCMPTRow, callback);
                PurchaseInvoiceCalculationRtn.calculateGst(tvPurInvCreateCMPTRow, callback);
                inx++;
            }
        }

        Platform.runLater(() -> {
            TableColumn<PurchaseInvoiceTable, ?> colName = tvPurInvCreateCMPTRow.getColumns().get(1);
            tvPurInvCreateCMPTRow.edit(redirectIndex, colName);
        });


    }

    public JsonObject productRedirectById(Long id) {

        Map<String, String> map = new HashMap<>();
        map.put("product_id", String.valueOf(id));

        String formData = mapToStringforFormData(map);
        HttpResponse<String> response = APIClient.postFormDataRequest(formData, "transaction_product_details");

        String json = response.body();
        JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
        JsonObject responseObj = jsonObject.getAsJsonObject("result");
        System.out.println("responseObj" + responseObj);
        String ledgerName = responseObj.get("productName").getAsString();
        //ledgerStateCode = responseObj.get("ledgerStateCode").getAsString();
//        JsonArray gstdetailsArray = responseObj.getAsJsonArray("gstdetails");
//        if (gstdetailsArray != null) {
//            for (JsonElement element : gstdetailsArray) {
//                JsonObject gstdetail = element.getAsJsonObject();
//                String gstin = gstdetail.get("gstin").getAsString();
//                String gst_id = gstdetail.get("id").getAsString();
//                supplierGSTINList.add(new CommonDTO(gstin, gst_id));
//            }
//        }
//        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
//            taxFlag = true;
//        }
        return responseObj;

    }

    public void handleCmbPaymentMode(ActionEvent actionEvent) {
        selectedPayMode = cmbRtnPaymentMode.getValue();

    }

    public void roundoffSelected(ActionEvent actionEvent) {
    }
}

class TextFieldTableCellForPurRtn extends TableCell<PurchaseInvoiceTable, String> {
    private final TextField textField;
    private final String columnName;

    private final TableCellCallback<Object[]> callback;
    private TableCellCallback<Object[]> productID_callback;

    TableCellCallback<Object[]> addPrdCalbak;

    private TextField button;


    public TextFieldTableCellForPurRtn(String columnName, TableCellCallback<Object[]> callback) {
        this.columnName = columnName;
        this.textField = new TextField();


        this.callback = callback;
        this.productID_callback = productID_callback;


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

        batchColumn();

        netAmountColumn();

    }

    public TextFieldTableCellForPurRtn(String columnName, TableCellCallback<Object[]> callback, TableCellCallback<Object[]> productID_callback, TextField button) {
        this.columnName = columnName;
        this.textField = new TextField();

        this.callback = callback;
        this.productID_callback = productID_callback;

        this.button = button;


        this.textField.setOnAction(event -> commitEdit(textField.getText()));

        // Commit when focus is lost
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(textField.getText());
            }
        });
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                this.textField.requestFocus();
                getTableView().getSelectionModel().select(getIndex());
                getTableView().getFocusModel().focus(getIndex());
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

        batchColumn();

        netAmountColumn();

    }


    public TextFieldTableCellForPurRtn(String columnName, TableCellCallback<Object[]> callback, TableCellCallback<Object[]> productID_callback, TextField button, TableCellCallback<Object[]> addPrdCalbak) {
        this.columnName = columnName;
        this.textField = new TextField();

        this.callback = callback;
        this.productID_callback = productID_callback;
        this.addPrdCalbak = addPrdCalbak;
        this.button = button;


        this.textField.setOnAction(event -> commitEdit(textField.getText()));

        // Commit when focus is lost
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(textField.getText());
            }
        });
        this.textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                this.textField.requestFocus();
                getTableView().getSelectionModel().select(getIndex());
                getTableView().getFocusModel().focus(getIndex());
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

        batchColumn();

        netAmountColumn();

    }

    private void batchColumn() {
        if ("tblcPurInvCreateCmptBatch".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    openBatchWindow();
                }
                if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(6);
                    getTableView().edit(getIndex(), colName);
                }

                if (event.getCode() == KeyCode.ENTER || !event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    if (getIndex() == getTableView().getItems().size() - 1) {
                        getTableView().getItems().add(new PurchaseInvoiceTable("", String.valueOf(getIndex() + 1), String.valueOf(getIndex() + 1), "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                    }
                    if (!textField.getText().isEmpty()) {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                        getTableView().edit(getIndex() + 1, colName);
                    } else {
                        textField.requestFocus();
                    }
                    event.consume();
                }
            });

            textField.setOnMouseClicked(event -> {
                openBatchWindow();
            });
        }
    }

    private void particularsColumn() {
        if ("tblcPurInvCreateCmptParticular".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    openProduct();
                }
                if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    if (getIndex() == 1) {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(7);
                        getTableView().edit(getIndex() - 1, colName);
                    } else {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(15);
                        getTableView().edit(getIndex() - 1, colName);
                    }
                }

                if (event.getCode() == KeyCode.ENTER || !event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    if (textField.getText().isEmpty() && getIndex() == getTableView().getItems().size() - 1 && getIndex() != 0) {
                        Platform.runLater(() -> {
                            button.requestFocus();
                        });
                    } else if (!textField.getText().isEmpty()) {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(6);
                        getTableView().edit(getIndex(), colName);
                    } else {
                        textField.requestFocus();
                    }
                    event.consume();
                }
            });

            textField.setOnMouseClicked(event -> {
                event.consume();
//                openProduct();
            });
        }
    }

    private void netAmountColumn() {

        if ("tblcPurInvCreateCmptNetAmt".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    int current_index = getTableRow().getIndex();

                    int serial_no = Integer.parseInt(getTableRow().getItem().getSr_no());

//                    getTableView().getItems().addAll(new PurchaseInvoiceTable("", String.valueOf(current_index + 1), String.valueOf(serial_no + 1), "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                }
            });
        }
    }

    private void openProduct() {

        //  System.out.println("pmk : "+getTableView().getItems().get(getIndex()).getParticulars());

        SingleInputDialogs.openProductPopUpForPurchase(Communicator.stage, getTableView().getItems().get(getIndex()).getParticulars(), "Product", input -> {

            String productName = (String) input[0];
            String productId = (String) input[1];
            String packaging = (String) input[2];
            String mrp = (String) input[3];
            String unit = (String) input[4];
            String taxper = (String) input[5];
            String salesrate = (String) input[6];
            String purchaseRate = (String) input[7];
            String is_batch = (String) input[8];
            /**** Multi Unit Rate : rateList contains unit wise rate ****/
            List<UnitRateList> rateList = new ArrayList<>();
            rateList.addAll((Collection<? extends UnitRateList>) input[9]);
            getTableRow().getItem().setRateList(rateList);
            /*if (productID_callback != null) {
                productID_callback.call(productId);
            }*/
            if (productID_callback != null) {
                Object[] object = new Object[1];
                object[0] = productId;
                productID_callback.call(object);
            }
            getTableRow().getItem().setParticulars(productName);
            getTableRow().getItem().setProduct_id(productId);
            getTableRow().getItem().setPackages(packaging);
            getTableRow().getItem().setIs_batch(is_batch);
            getTableRow().getItem().setTax(taxper);

            List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPackingRtn.getAllProductUnitsPackingRtnFlavour(productId);
            int index = getTableRow().getIndex();
            ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);

            System.out.println("levelAForPurInvoiceList >> : " + levelAForPurInvoiceList);
            System.out.println("observableLevelAList >> : " + observableLevelAList);
            System.out.println("index >> : " + index);
            System.out.println("PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size() >> : " + PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size());

            if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);
                getTableRow().getItem().setLevelA(null);
                getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());


            } else {
                PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
                getTableRow().getItem().setLevelA(null);
                getTableRow().getItem().setLevelA(levelAForPurInvoiceList.get(0).getLabel());

            }

            TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
            getTableView().edit(getIndex(), colName);
        }, item -> {
            if (item) {
                if (addPrdCalbak != null) {
                    int cur_index = getTableRow().getIndex();
                    Object[] object = new Object[3];
                    object[0] = true;
                    object[1] = cur_index;
                    object[2] = "isProduct";
                    addPrdCalbak.call(object);
                }
            }
        });
    }

    public void openBatchWindow() {
        int current_index = getTableRow().getIndex();
        List<PurchaseInvoiceTable> tableList = getTableView().getItems();

        List<BatchWindowTableDTO> batchWindowTableDTOList;
        PurchaseInvoiceTable current_table_row = tableList.get(current_index);
        batchWindowTableDTOList = current_table_row.getBatchWindowTableDTOList();
        Map<String, String> product_details_to_batch_window = new HashMap<>();
        product_details_to_batch_window.put("product_id", current_table_row.getProduct_id());
        product_details_to_batch_window.put("product_name", current_table_row.getParticulars());
        product_details_to_batch_window.put("levelA_id", current_table_row.getLevelA_id());
        product_details_to_batch_window.put("levelB_id", current_table_row.getLevelB_id());
        product_details_to_batch_window.put("levelC_id", current_table_row.getLevelC_id());
        product_details_to_batch_window.put("unit_id", current_table_row.getUnit_id());
        product_details_to_batch_window.put("tax", current_table_row.getTax());
        product_details_to_batch_window.put("ledger_id", getTableView().getItems().get(0).getLedger_id());

        /**** get the PTR & CSR Rates using current row index from table ****/
        List<UnitRateList> rateList = current_table_row.getRateList();
        for (UnitRateList unitRateList : rateList) {
            if (unitRateList.getUnitId() == Long.parseLong(current_table_row.getUnit_id())) {
                product_details_to_batch_window.put("fsrmh", "" + unitRateList.getFsrmh());
                product_details_to_batch_window.put("fsrai", "" + unitRateList.getFsrai());
                product_details_to_batch_window.put("csrmh", "" + unitRateList.getCsrmh());
                product_details_to_batch_window.put("csrai", "" + unitRateList.getCsrai());
                product_details_to_batch_window.put("mrp", unitRateList.getMrp() == 0.0 ? "" : String.valueOf(unitRateList.getMrp()));
                product_details_to_batch_window.put("purchaserate", unitRateList.getPurRate() == 0.0 ? "" : String.valueOf(unitRateList.getPurRate()));
            }
        }

        Long ledgerid = Long.parseLong(getTableView().getItems().get(0).getLedger_id());

        if (current_table_row.getIs_batch().equals("true")) {
            BatchWindow.batchWindow(String.valueOf(current_index), product_details_to_batch_window, batchWindowTableDTOList, input -> {

                getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setBatchWindowTableDTOList(input);

                for (BatchWindowTableDTO batchWindowTableDTOSs : input) {
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setBatch_or_serial(batchWindowTableDTOSs.getBatchNo());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setQuantity(batchWindowTableDTOSs.getQuantity());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setFree(batchWindowTableDTOSs.getFree());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate(batchWindowTableDTOSs.getPurchaseRate());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setDis_per(batchWindowTableDTOSs.getDiscountPercentage());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setDis_amt(batchWindowTableDTOSs.getDiscountAmount());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_details_id(batchWindowTableDTOSs.getB_details_id());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_no(batchWindowTableDTOSs.getBatchNo());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_rate(batchWindowTableDTOSs.getMrp());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_purchase_rate(batchWindowTableDTOSs.getPurchaseRate());

                    double purchase_rate = Double.parseDouble(batchWindowTableDTOSs.getPurchaseRate());

                    double discount_per = 0.0;
                    if (!batchWindowTableDTOSs.getDiscountPercentage().isEmpty()) {
                        discount_per = Double.parseDouble(batchWindowTableDTOSs.getDiscountPercentage());
                    }

                    double discount_amt = 0.0;

                    if (!batchWindowTableDTOSs.getDiscountAmount().isEmpty()) {
                        discount_amt = Double.parseDouble(batchWindowTableDTOSs.getDiscountAmount());
                    }

                    double total_batch_discount = (purchase_rate * discount_per) / 100;

                    total_batch_discount = total_batch_discount + discount_amt;

                    double costing_without_tax = purchase_rate - total_batch_discount;

                    double current_row_tax = Double.parseDouble(getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).getTax());

                    double tax_amt_of_brate = (costing_without_tax * current_row_tax) / 100;

                    double costing_with_tax = costing_without_tax + tax_amt_of_brate;


                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setCosting(String.valueOf(costing_without_tax));

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setCosting_with_tax(String.valueOf(costing_with_tax));


                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_expiry(String.valueOf(Communicator.text_to_date.fromString(batchWindowTableDTOSs.getExpiryDate())));

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_a(batchWindowTableDTOSs.getFsr_mh().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_b(batchWindowTableDTOSs.getFsr_ai().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_ai());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_c(batchWindowTableDTOSs.getCsr_mh().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_d(batchWindowTableDTOSs.getCsr_ai().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_ai());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setMin_margin(batchWindowTableDTOSs.getMargin());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setManufacturing_date(String.valueOf(Communicator.text_to_date.fromString(batchWindowTableDTOSs.getManufacturingDate())));

                }
                String batchNo = getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).getBatch_or_serial();
                String bDetailsId = getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).getB_details_id();
                if (productID_callback != null) {
                    Object[] object = new Object[2];
                    object[0] = batchNo;
                    object[1] = bDetailsId;
                    productID_callback.call(object);
                }
                PurchaseInvoiceCalculationRtn.rowCalculationForPurcInvoice(Integer.parseInt(input.get(0).getCurrentIndex()), getTableView(), callback);

                PurchaseInvoiceCalculationRtn.calculateGst(getTableView(), callback);
                getTableView().getItems().addAll(new PurchaseInvoiceTable("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));

                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                System.out.println("Col Name:" + colName.getText() + "Index:" + getIndex());
                getTableView().edit(getIndex() + 1, colName);
            });
        }
    }

    public void textfieldStyle() {
        if (columnName.equals("tblcPurInvCreateCmptParticular")) {
            textField.setPromptText("Particulars");
            textField.setEditable(false);
        } else {
            textField.setPromptText("0");
        }

        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            textField.setPrefHeight(38);
            textField.setMaxHeight(38);
            textField.setMinHeight(38);
        } else if (height == 1050) {
            textField.setPrefHeight(38);
            textField.setMaxHeight(38);
            textField.setMinHeight(38);
        } else if (height == 1024) {
            textField.setPrefHeight(32);
            textField.setMaxHeight(32);
            textField.setMinHeight(32);
        } else if (height == 960) {
            textField.setPrefHeight(32);
            textField.setMaxHeight(32);
            textField.setMinHeight(32);
        } else if (height == 900) {
            textField.setPrefHeight(32);
            textField.setMaxHeight(32);
            textField.setMinHeight(32);
        } else if (height == 800) {
            textField.setPrefHeight(27.5);
            textField.setMaxHeight(27.5);
            textField.setMinHeight(27.5);
        } else if (height == 768) {

        } else if (height == 720) {
        }


        if (columnName.equals("tblcPurInvCreateCmptParticular")) {
            textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    textField.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 2; -fx-border-color: #00a0f5;");
                } else {
                    textField.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 2; -fx-border-color: #f6f6f9;");
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
        }

        if (columnName.equals("tblcPurInvCreateCmptNetAmt")) {
            this.textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                    getTableView().getItems().addAll(new PurchaseInvoiceTable("", "", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                }
            });
        }
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
        setText(getItem());
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
            textField.setText(item);
            setGraphic(vbox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        Object item = getTableRow().getItem();
        if (item != null && columnName.equals("tblcPurInvCreateCmptParticular")) {
            ((PurchaseInvoiceTable) item).setParticulars(newValue.isEmpty() ? "" : newValue);
        } else if (columnName.equals("tblcPurInvCreateCmptBatch")) {
            (getTableRow().getItem()).setBatch_or_serial(newValue.isEmpty() ? "" : newValue);
        }
    }
}

class ComboBoxTableCellForLevelARtn extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelARtn(String columnName) {

        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1050) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1024) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
        } else if (height == 960) {

        } else if (height == 900) {

        } else if (height == 800) {

        } else if (height == 768) {

        } else if (height == 720) {
        }

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
        setText(getItem());
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
        TableRow<PurchaseInvoiceTable> row = getTableRow();
        if (row != null) {
            PurchaseInvoiceTable item = row.getItem();
            if (item != null) {
                item.setLevelA(newValue);
            }
        }
    }

}

class ComboBoxTableCellForLevelBRtn extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelBForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelBRtn(String columnName) {
        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1050) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1024) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
        } else if (height == 960) {

        } else if (height == 900) {

        } else if (height == 800) {

        } else if (height == 768) {

        } else if (height == 720) {
        }


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
        setText(getItem());
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

class ComboBoxTableCellForLevelCRtn extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelCRtn(String columnName) {


        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");


        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1050) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
        } else if (height == 1024) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
        } else if (height == 960) {

        } else if (height == 900) {

        } else if (height == 800) {

        } else if (height == 768) {

        } else if (height == 720) {
        }

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
        setText(getItem());
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

class ComboBoxTableCellForUnitRtn extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;

    public ComboBoxTableCellForUnitRtn(String columnName) {


        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");


        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });
        } else if (height == 1050) {
            comboBox.setPrefHeight(38);
            comboBox.setMaxHeight(38);
            comboBox.setMinHeight(38);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });
        } else if (height == 1024) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });

        } else if (height == 960) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });
        } else if (height == 900) {
            comboBox.setPrefHeight(32);
            comboBox.setMaxHeight(32);
            comboBox.setMinHeight(32);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });
        } else if (height == 800) {
            comboBox.setPrefHeight(27.5);
            comboBox.setMaxHeight(27.5);
            comboBox.setMinHeight(27.5);
            Platform.runLater(() -> {
                comboBox.setPrefWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMaxWidth(getTableView().getColumns().get(6).getWidth() - 1);
                comboBox.setMinWidth(getTableView().getColumns().get(6).getWidth() - 1);
            });
        } else if (height == 768) {

        } else if (height == 720) {
        }


        this.comboBox.setOnShowing(event -> {
            Node node = this.comboBox.lookup(".list-view");
            if (node instanceof ListView<?>) {
                ((ListView<?>) node).setMaxHeight(120);
                ((ListView<?>) node).setMinHeight(120);
                ((ListView<?>) node).setPrefHeight(120);
            }
        });

        this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");

        this.comboBox.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                this.comboBox.setStyle("-fx-background-color: #fff9c4; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #00a0f5;");
                comboBox.show();
            } else {
                this.comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1.5; -fx-border-color: #f6f6f9;");
            }
        });

//        if (comboList != null) {
//            for (LevelAForPurInvoice commonDTO : comboList) {
//                this.comboBox.getItems().add(commonDTO.getLabel());
//            }
//        }
        this.comboBox.setFocusTraversable(true);


        this.comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEdit(String.valueOf(comboBox.getValue()));
            }
        });


//        getTableView().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                int selectedIndex = newValue.intValue();
//                if (selectedIndex >= 0) {
//                    comboBox.setStyle("-fx-background-color: #e6f5ff; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
//                } else {
//                    comboBox.setStyle("-fx-background-color: #f6f6f9; -fx-border-radius: 0px; -fx-border-width: 1; -fx-border-color: #f6f6f9;");
//                }
//            }
//        });

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
        comboBox.setOnKeyPressed(event -> {
            if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                getTableView().edit(getIndex(), colName);
            }

            if (event.getCode() == KeyCode.ENTER || !event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(7);
                getTableView().edit(getIndex(), colName);
            }
//            if (event.getCode() == KeyCode.DOWN) {
//                comboBox.show();
//                event.consume();
//            }
        });

    }

    @Override
    public void startEdit() {
        super.startEdit();
        setText(null);
        setGraphic(comboBox);
        comboBox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(comboBox);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            if (getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                setGraphic(comboBox);
            }
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

            setGraphic(comboBox);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        (getTableRow().getItem()).setUnit(newValue);
    }

}


class ButtonTableCellDeleteRtn extends TableCell<PurchaseInvoiceTable, String> {
    private final Button delete;
    TableCellCallback<Object[]> callback;

    public ButtonTableCellDeleteRtn(TableCellCallback<Object[]> DelCallback) {


        this.delete = createButtonWithImage();

        callback = DelCallback;

        delete.setOnAction(actionEvent -> {
            PurchaseInvoiceTable table = getTableView().getItems().get(getIndex());
            getTableView().getItems().remove(table);
            Object object[] = new Object[1];
            if (callback != null) {
                object[0] = table.getDetails_id();
                callback.call(object);
            }
            getTableView().refresh();
        });


    }


    private Button createButtonWithImage() {

        ImageView imageView = new ImageView(new Image(GenivisApplication.class.getResourceAsStream("/com/opethic/genivis/ui/assets/del.png")));
        Button button = new Button();

        double height = Screen.getPrimary().getBounds().getHeight();

        if (height == 1080) {
            imageView.setFitWidth(28);
            imageView.setFitHeight(28);

            button.setMaxHeight(38);
            button.setPrefHeight(38);
            button.setMinHeight(38);
            button.setPrefWidth(35);
            button.setMaxWidth(35);
            button.setMinWidth(35);
            button.setGraphic(imageView);
        } else if (height == 1050) {
            imageView.setFitWidth(28);
            imageView.setFitHeight(28);

            button.setMaxHeight(38);
            button.setPrefHeight(38);
            button.setMinHeight(38);
            button.setPrefWidth(35);
            button.setMaxWidth(35);
            button.setMinWidth(35);
            button.setGraphic(imageView);
        } else if (height == 1024) {

            imageView.setFitWidth(26);
            imageView.setFitHeight(26);

            button.setMaxHeight(32);
            button.setPrefHeight(32);
            button.setMinHeight(32);
            button.setPrefWidth(35);
            button.setMaxWidth(35);
            button.setMinWidth(35);
            button.setGraphic(imageView);

        } else if (height == 960) {
            imageView.setFitWidth(26);
            imageView.setFitHeight(26);

            button.setMaxHeight(32);
            button.setPrefHeight(32);
            button.setMinHeight(32);
            button.setPrefWidth(35);
            button.setMaxWidth(35);
            button.setMinWidth(35);
            button.setGraphic(imageView);
        } else if (height == 900) {
            imageView.setFitWidth(26);
            imageView.setFitHeight(26);

            button.setMaxHeight(32);
            button.setPrefHeight(32);
            button.setMinHeight(32);
            button.setPrefWidth(35);
            button.setMaxWidth(35);
            button.setMinWidth(35);
            button.setGraphic(imageView);
        } else if (height == 800) {

        } else if (height == 768) {

        } else if (height == 720) {
        }


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

class ProductUnitsPackingRtn {

    public static List<LevelAForPurInvoice> getAllProductUnitsPackingRtnFlavour(String product_id) {


        Map<String, String> map = new HashMap<>();
        map.put("product_id", product_id);

        List<LevelAForPurInvoice> levelAForPurInvoicesList = new ArrayList<>();
        try {
            String formdata = mapToStringforFormData(map);

            HttpResponse<String> response = APIClient.postFormDataRequest(formdata, "get_all_product_units_packings_flavour");
            String responseBody = response.body();
            System.out.println("responseBody mk: " + responseBody);
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);

            int responseStatus = jsonObject.get("responseStatus").getAsInt();
            System.out.println("responseStatus: " + responseStatus);

            if (responseStatus == 200) {
                JsonObject resObj = jsonObject.get("responseObject").getAsJsonObject();
                JsonArray lstPackages = resObj.get("lst_packages").getAsJsonArray();


                for (JsonElement lstPackage : lstPackages) {
                    LevelAForPurInvoice levelAForPurInvoice = new Gson().fromJson(lstPackage, LevelAForPurInvoice.class);
                    levelAForPurInvoicesList.add(levelAForPurInvoice);
                }


            } else {
                System.out.println("responseObject is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return levelAForPurInvoicesList;
    }

}

class PurchaseInvoiceCalculationRtn {

    public static void rowCalculationForPurcInvoice(int rowIndex, TableView<PurchaseInvoiceTable> tableView, TableCellCallback<Object[]> callback) {
        PurchaseInvoiceTable purchaseInvoiceTable = tableView.getItems().get(rowIndex);
        if (!purchaseInvoiceTable.getQuantity().isEmpty() && Double.parseDouble(purchaseInvoiceTable.getQuantity()) > 0 && !purchaseInvoiceTable.getRate().isEmpty() && Double.parseDouble(purchaseInvoiceTable.getRate()) > 0) {

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
            if (purchaseInvoiceTable.getDis_per2() != null && !purchaseInvoiceTable.getDis_per2().isEmpty()) {
                disPer2 = Double.parseDouble(purchaseInvoiceTable.getDis_per2());
            } else {
                disPer2 = 0.0;
            }
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
            PurchaseInvoiceTable selectedItem = tableView.getItems().get(rowIndex);
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
//        selectedItem.setGross_amt(String.valueOf(gross_amt));
            selectedItem.setDis_per(String.valueOf((r_dis_per)));
            selectedItem.setDis_per_cal(String.valueOf(total_dis_per));
            selectedItem.setDis_amt(String.valueOf((r_dis_amt)));
            selectedItem.setDis_amt_cal(String.valueOf(r_dis_amt));
            selectedItem.setRow_dis_amt(String.valueOf(row_dis_amt));
            selectedItem.setGross_amount1(String.valueOf(taxable_amt));
            selectedItem.setTotal_taxable_amt(String.valueOf(taxable_amt));

            // tax percentage store cmptrow
            selectedItem.setGst(String.valueOf(r_tax_per));
            selectedItem.setIgst_per(String.valueOf(r_tax_per));
            selectedItem.setSgst_per(String.valueOf(r_tax_per / 2));
            selectedItem.setCgst_per(String.valueOf(r_tax_per / 2));
            selectedItem.setTotal_igst(String.valueOf(totalTax));
            selectedItem.setTotal_sgst(String.valueOf(totalTax / 2));
            selectedItem.setTotal_cgst(String.valueOf(totalTax / 2));
            selectedItem.setFinal_amount(String.valueOf(net_amt));
            selectedItem.setDis_per2(String.valueOf(disPer2));
        }
    }

    public static void calculateGst(TableView<PurchaseInvoiceTable> tableView, TableCellCallback<Object[]> callback) {


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
        for (PurchaseInvoiceTable purchaseInvoiceTable : tableView.getItems()) {
            if (!purchaseInvoiceTable.getQuantity().isEmpty() && Double.parseDouble(purchaseInvoiceTable.getQuantity()) > 0 && !purchaseInvoiceTable.getRate().isEmpty() && Double.parseDouble(purchaseInvoiceTable.getRate()) > 0) {
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
                Double igst = Double.parseDouble(purchaseInvoiceTable.getIgst());
                Double cgst = Double.parseDouble(purchaseInvoiceTable.getCgst());
                Double sgst = Double.parseDouble(purchaseInvoiceTable.getSgst());
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
                taxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                totaltaxableAmt += taxableAmt;
                disAmt = Double.parseDouble(purchaseInvoiceTable.getFinal_dis_amt());
                totalDisAmt += disAmt;
                taxAmt = Double.parseDouble(purchaseInvoiceTable.getFinal_tax_amt());
                totalTaxAmt += taxAmt;

            }
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
        }


        if (callback != null) {

            Object[] object = new Object[10];

            object[0] = decimalFormat.format(totalQuantity);

            object[1] = String.valueOf(totalFreeQuantity);

            object[2] = String.format("%.2f", totalNetAmt);

            object[3] = String.format("%.2f", totalGrossAmt);

            object[4] = String.format("%.2f", totalDisAmt);

            //  lblPurRtnTotalTaxableAmount.setText(String.format("%.2f", (totalGrossAmt - totalDisAmt + addChargAmount)));
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


    public static void discountPropotionalCalculation(String disproportionalPer, String disproportionalAmt, String additionalCharges, TableView<PurchaseInvoiceTable> tableView, TableCellCallback<Object[]> callback) {

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
//            rowCalculationForPurcInvoice(i, tableView, callback);//call row calculation function
            PurchaseInvoiceTable purchaseInvoiceTable = tableView.getItems().get(i);
            totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
        }
        System.out.println("totalTaxableAmt discountPropotionalCalculation :: " + totalTaxableAmt);

        Double rowDisc = 0.0;
        //get data of netamount, tax per,and row taxable amount from cmptrow
        for (PurchaseInvoiceTable purchaseInvoiceTable : tableView.getItems()) {
            System.out.println("getFinal_dis_amt--->" + purchaseInvoiceTable.getFinal_dis_amt());
            rowDisc = purchaseInvoiceTable.getFinal_dis_amt() != null && !purchaseInvoiceTable.getFinal_dis_amt().isEmpty() ? Double.parseDouble(purchaseInvoiceTable.getFinal_dis_amt()) : 0.0;
            System.out.println("netAmt--->" + purchaseInvoiceTable.getNet_amount());
            netAmt = purchaseInvoiceTable.getNet_amount() != null ? Double.parseDouble(purchaseInvoiceTable.getNet_amount()) : 0.0;
            System.out.println("netAmt--->" + purchaseInvoiceTable.getTax());
            r_tax_per = Double.parseDouble(purchaseInvoiceTable.getTax());
            rowTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
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

            //? Bill Level Disc Amount Calculation
            if (dis_proportional_amt > 0.0) {
                totalDisPropAmt = dis_proportional_amt;
                rowDisPropAmt = (rowTaxableAmt * totalDisPropAmt) / totalTaxableAmt;
                rowDisc += rowDisPropAmt;
                rowTaxableAmt = rowTaxableAmt - rowDisPropAmt;
                totalTaxableAmtAdditional = rowTaxableAmt;
            } else {
                totalTaxableAmtAdditional = rowTaxableAmt;

            }


            //calculate tax per and store the data to final net AMount
            if (additionalCharges.isEmpty()) {
                if (r_tax_per >= 0) {
                    total_tax_amt = (r_tax_per / 100) * (rowTaxableAmt);
                    System.out.println("total_tax_amt" + total_tax_amt);
                    netAmt = rowTaxableAmt + total_tax_amt;
                    System.out.println("total netAmt  :" + netAmt + rowTaxableAmt);
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
//        calculateGst(tableView, callback);
    }


    public static void additionalChargesCalculation(String additionalCharges, TableView<PurchaseInvoiceTable> tableView, TableCellCallback<Object[]> callback) {


        Double additionalChargeAmt = 0.0;//store data textfield string to double

        if (!additionalCharges.isEmpty()) {
            additionalChargeAmt = Double.parseDouble(additionalCharges);//convert string to double


            Double finalNetAmt = 0.0;
            Double r_tax_per = 0.0;
            Double total_tax_amt = 0.0;
            Double netAmt = 0.0;
            Double rowTaxableAmt = 0.0;
            Double totalTaxableAmt = 0.0;
            Double totalDisPropAmt = 0.0;
            Double rowDisPropAmt = 0.0;

            for (int i = 0; i < tableView.getItems().size(); i++) {
                PurchaseInvoiceTable purchaseInvoiceTable = tableView.getItems().get(i);
                totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTotal_taxable_amt());

            }
            for (PurchaseInvoiceTable purchaseInvoiceTable : tableView.getItems()) {

                System.out.println("row ->" + purchaseInvoiceTable.toString());
                netAmt = Double.parseDouble(purchaseInvoiceTable.getNet_amount());
                System.out.println("netAMout >> : " + netAmt);
                r_tax_per = Double.parseDouble(purchaseInvoiceTable.getTax());
                rowTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTotal_taxable_amt());
                System.out.println("rowTaxableAmt >> : " + rowTaxableAmt);
                System.out.println("GRoss AMT " + purchaseInvoiceTable.getGross_amount());


                totalDisPropAmt = additionalChargeAmt;
                rowDisPropAmt = (rowTaxableAmt * totalDisPropAmt) / totalTaxableAmt;
                rowTaxableAmt = rowTaxableAmt + rowDisPropAmt;

                if (r_tax_per >= 0) {
                    total_tax_amt = (r_tax_per / 100) * (rowTaxableAmt);
                    netAmt = rowTaxableAmt + total_tax_amt;
                }
//                purchaseInvoiceTable.setGross_amount();

                purchaseInvoiceTable.setNet_amount(String.format("%.3f", netAmt));
                purchaseInvoiceTable.setCgst(String.valueOf(total_tax_amt / 2));
                purchaseInvoiceTable.setSgst(String.valueOf(total_tax_amt / 2));
                purchaseInvoiceTable.setFinal_tax_amt(String.valueOf(total_tax_amt));


            }

        }
        //call calculate gst function
        calculateGst(tableView, callback);

    }


}








