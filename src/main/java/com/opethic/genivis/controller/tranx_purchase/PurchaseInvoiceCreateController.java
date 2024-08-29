package com.opethic.genivis.controller.tranx_purchase;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.controller.dialogs.AdditionalCharges;
import com.opethic.genivis.controller.dialogs.BatchWindow;
import com.opethic.genivis.controller.dialogs.SingleInputDialogs;
import com.opethic.genivis.controller.tranx_sales.common.LineNumbersCellFactory;
import com.opethic.genivis.dto.*;
import com.opethic.genivis.dto.add_charges.CustomAddChargesDTO;
import com.opethic.genivis.dto.cmp_t_row.BatchWindowTableDTO;
import com.opethic.genivis.dto.pur_invoice.*;
import com.opethic.genivis.dto.pur_invoice.reqres.*;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.dto.reqres.product.TableCellCallback;
import com.opethic.genivis.dto.reqres.pur_tranx.AdditionalCharge;
import com.opethic.genivis.dto.reqres.pur_tranx.PurTranxToProductRedirectionDTO;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.opethic.genivis.utils.FxmFileConstants.PURCHAE_INV_LIST_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.SALES_QUOTATION_LIST_SLUG;
import static com.opethic.genivis.utils.Globals.decimalFormat;
import static com.opethic.genivis.utils.Globals.mapToStringforFormData;
import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;
import static javafx.scene.control.PopupControl.USE_PREF_SIZE;

public class PurchaseInvoiceCreateController implements Initializable {
    @FXML
    public TabPane tranxPurInvTabPane;
    @FXML
    public Tab tabPurInvLedger;
    @FXML
    public Tab tabPurInvProduct;
    @FXML
    public Label lblPurInvMarginPer;
    @FXML
    public Label lblPurInvFsrmh;

    @FXML
    public Label lblPurInvPurRate;
    @FXML
    public Label lblPurInvMfgDate;

    @FXML
    public Label lblPurInvBatchNo;
    @FXML
    public Tab tabPurInvBatch;
    @FXML
    public Label lbPurInvBatchCsrai;
    @FXML
    public Label lblPurInvExpDate;
    @FXML
    public Label lbPurInvBatchCsrmh;
    @FXML
    public Label lblPurInvMrp;
    @FXML
    public Label lblPurInvFsrai;
    @FXML
    public Label lblPurInvBarcode;
    @FXML
    public Label lblPurInvFreeqty;
    @FXML
    public Label lblPurInvCost;
    @FXML
    public Label lblPurInvCostWithTax;
    @FXML
    public CheckBox chbPurInvRoundOff;
    @FXML
    public Label lblPurInvRoundOff, lbtotalQty, lbtotalFreeQty;
    @FXML
    public ComboBox<String> cmbPaymentMode;
    @FXML
    public Button btnSubmit;
    @FXML
    public Button btnModify;

    @FXML
    public HBox bottom_hbox;
    @FXML
    public TextField tfNarration;

    @FXML
    public Label lbTranxDate, lbLedgerName, lbSuplierGSTN, lbPurchaseSerial, lbInvoiceNo, lbInvoiceDate, lbPurchaseAc, lbPaymentMethod, lbPurchaseOrder, lbPurchaseChallan;

    @FXML
    public HBox header_hbox_1, header_hbox_2;

    @FXML
    public VBox vbox_header;

    @FXML
    public HBox hbox_batch_tab, hbox_ledger_tab, hbox_product_tab;

    @FXML
    public HBox main_bottom_hbox;

    @FXML
    public VBox amounts_bottom_section;
    public Label purc_orders,purc_challan;
    public Button add_charges;

    private ObservableList<String> payment_mode_list = FXCollections.observableArrayList("Credit", "Cash");

    @FXML
    ScrollPane purScrollPane;

    @FXML
    private TableView<GstDTO> tvGST_Table;

    @FXML
    private TableColumn<GstDTO, String> tc_gst;

    @FXML
    private TableColumn<GstDTO, String> tc_cgst;

    @FXML
    private TableColumn<GstDTO, String> tc_sgst;
    @FXML
    private TableColumn<GstDTO, String> tc_igst;


    @FXML
    private TableView<PurchaseInvoiceTable> tvPurchaseInvoiceTable;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcSrNo;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcParticulars;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcPackage;


    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcLevelA;


    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcLevelB;


    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcLevelC;


    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcUnit;


    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcBatch;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcQuantity;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcRate;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcGrossAmount;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcDisPer;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcDisAmt;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcTax;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcNetAmount;

    @FXML
    private TableColumn<PurchaseInvoiceTable, String> tcAction;
    @FXML
    public TableColumn<PurchaseInvoiceTable, String> tcFreeQty;

    JSONArray rowDelDetailsIds = new JSONArray();

    private final ObservableList<LevelAForPurInvoice> levelAList = FXCollections.observableArrayList();

    private final ObservableList<LevelBForPurInvoice> levelBList = FXCollections.observableArrayList();

    private final ObservableList<CommonDTO> levelCList = FXCollections.observableArrayList();

    private final ObservableList<CommonDTO> unitList = FXCollections.observableArrayList();

    @FXML
    private TextField tfTranxDate, tfInvoiceNo, tfLedgerName, tfInvoiceDate, tfPurchaseSerial;

    @FXML
    private ComboBox<CommonDTO> cbPurchaseAc;

    @FXML
    private ComboBox<CommonDTO> cbSupplierGSTIN;

    String purchaseAcID = "";

    String supplierGSTIN_ID = "";

    String ledger_id = "";

//    @FXML
//    RadioButton rbCredit, rbCash;

    private final Logger logger = LogManager.getLogger(PurchaseInvoiceCreateController.class);

    private final ObservableList<CommonDTO> purchaseAcList = FXCollections.observableArrayList();

    private final ObservableList<CommonDTO> supplierGSTINList = FXCollections.observableArrayList();

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private String selectedPayMode = "Credit";

    @FXML
    private Label lbGrossTotal, lbDiscount, lbTotal, lbTax, lbBillAmount;

    private Map<String, String> purchase_invoice_map = new HashMap<>();
    private Map<String, String> deleted_rows_map = new HashMap<>();

    private String ledgerStateCode = "";
    private Boolean taxFlag = false;

    private String totalamt = "";

    private Double total_taxable_amt = 0.0;

    private String total_purchase_discount_amt = "";

    private Integer rediCurrIndex = -1;
    private Boolean isProductRed = false;
    private Boolean isLedgerRed= false,isRedirect=false;
    String purchaseRedId= "";

    @FXML
    private TextField purchase_discount, purchase_discount_amt, tfAddCharges;
    String gst_no = "";
    List<CustomAddChargesDTO> addChargesDTOList = new ArrayList<>();
    private final List<AddChargesListDTO> addChargesResLst = new ArrayList<>();

    @FXML
    private TableView<InvoiceProductHistoryTable> tvInvoiceProductHistory;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmSupplierName;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmInvNo;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmInvDate;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmBatch;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmMRP;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmQty;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmRate;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmCost;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmDisPer;

    @FXML
    private TableColumn<InvoiceProductHistoryTable, String> cmDisAmt;

    private Integer purchase_id = -1;
    private Integer purPrdRedId = -1;

    private final Map<String, Object> edit_response_map = new HashMap<>();

    private final Map<String, String> invoice_data_map = new HashMap<>();

    private final List<RowListDTO> rowListDTOS = new ArrayList<>();


    @FXML
    private Label lblPurInvGstNo, lblPurInvArea, lblPurInvBank, lblPurInvContactPerson, lblPurInvTransportName, lblPurInvCreditDays, lblPurInvFssai, lblPurInvLicenseNo, lblPurInvRoute, lblPurInvProductBrand, lblPurInvProductGroup, lblPurInvProductCategory, lblPurInvProductSubGroup, lblPurInvProductHsn, lblPurInvProductTaxType, lblPurInvProductTaxPer, lblPurInvProductMarginPer, lblPurInvProductCost, lblPurInvProductShelfId, lblPurInvProductMinStock, lblPurInvProductMaxStock;
    private Long stateCode = 0L, CompanyStateCode = 0L;
    private JsonArray deletedRows = new JsonArray();

    private int actionPerformed = 0;
    public static Integer selectedRowIndex;
    Integer batchIdToDelete;

    public void setEditId(Integer id) {
        System.out.println("id 1 " + id);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tvPurchaseInvoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int selectedIndex = tvPurchaseInvoiceTable.getSelectionModel().getSelectedIndex();
                tvPurchaseInvoiceTable.scrollTo(selectedIndex);
              //  System.out.println("Index: " + selectedIndex);
            }
        });

        resolution();

        // autofocus on TranxDate
        Platform.runLater(() -> {
            tfLedgerName.requestFocus();
        });

        //confirmation popup if invoice date is less than current date
        CommonFunctionalUtils.tranxDateGreaterThanCurrentDate1(tfInvoiceDate, cbPurchaseAc);

        initialEnterMethod();

        PurInvoiceCommunicator.levelAForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.levelBForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.levelCForPurInvoiceObservableList.clear();
        PurInvoiceCommunicator.unitForPurInvoiceList.clear();




        getPaymentModes();
        purchase_id = PurchaseInvoiceListController.getPurchaseId();
        System.out.println("purchase  id : " + purchase_id);
        PurchaseInvoiceListController.purchase_id = -1;
        if (purchase_id > 0) {
            btnSubmit.setText("Update");
            btnModify.setVisible(false);
            getPurchaseInvoiceByIdNew(purchase_id);
        }



        tvInvoiceProductHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setGSTFooterPart();
        sceneInitilization();

        tvGST_Table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        purScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tfLedgerName.setEditable(true);

        tableInitiliazation();

        cmbPaymentMode.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvPurchaseInvoiceTable.edit(0, tvPurchaseInvoiceTable.getColumns().get(1));
            }
        });

        if (purchase_id > 0) {
            tfPurchaseSerial.setText(invoice_data_map.get("purchase_sr_no"));
           tfNarration.setText((String) edit_response_map.get("narration"));
        } else {
            getPurchaseSerial();
        }

        add_charges.setOnKeyPressed(event->{
            if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB && !event.isShiftDown()){
                btnSubmit.requestFocus();
                event.consume();
            }
        });


        tfPurchaseSerial.setEditable(false);
        tfPurchaseSerial.setFocusTraversable(false);

        tfTranxDate.setFocusTraversable(false);
        tfTranxDate.setEditable(false);


        LocalDate current_date = LocalDate.now();

        if (purchase_id > 0) {
            LocalDate transaction_dt = LocalDate.parse(invoice_data_map.get("transaction_dt"));
            tfTranxDate.setText(transaction_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            LocalDate invoice_dt = LocalDate.parse(invoice_data_map.get("invoice_dt"));
            tfInvoiceDate.setText(invoice_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            tfTranxDate.setText(current_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            DateValidator.applyDateFormat(tfInvoiceDate);
            LocalDate currentDate = LocalDate.now();
            tfInvoiceDate.setText(currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            sceneInitilization();
        }

        cbPurchaseAc.setFocusTraversable(true);

        cbSupplierGSTIN.setFocusTraversable(true);

        getPurchaseAc();
        tfLededgerNameEvent();
        AutoCompleteBox<CommonDTO> autoCompleteBox = new AutoCompleteBox<>(cbSupplierGSTIN, -1);
        if (purchase_id > 0) {

            System.out.println("invoice_data_map : " + invoice_data_map.get("supplierId"));
            tfLedgerName.setText(ledgersById(Long.valueOf(invoice_data_map.get("supplierId"))));
            if (!supplierGSTINList.isEmpty()) {
                cbSupplierGSTIN.setItems(supplierGSTINList);
                cbSupplierGSTIN.setValue(supplierGSTINList.get(0));
            }
        }

        if (purchase_id > 0) {
            tfInvoiceNo.setText(invoice_data_map.get("invoice_no"));
        }

        if (purchase_id > 0) {

            for (CommonDTO commonDTO : purchaseAcList) {
                if (commonDTO.getId().equals(invoice_data_map.get("purchase_account_ledger_id"))) {
                    cbPurchaseAc.setValue(commonDTO);
                    purchaseAcID = commonDTO.getId();
                }
            }
        }

        if (purchase_id > 0) {
            Float dis_per_float = (Float) edit_response_map.get("discountInPer");
            purchase_discount.setText(String.valueOf(dis_per_float));
            String discText = purchase_discount.getText();
            if (!TextUtils.isEmpty(discText)) {
                double totalTaxableAmt = 0.0;
                for (int i = 0; i < tvPurchaseInvoiceTable.getItems().size(); i++) {
                    PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(i, tvPurchaseInvoiceTable, callback);//call row calculation function
                    PurchaseInvoiceTable purchaseInvoiceTable = tvPurchaseInvoiceTable.getItems().get(i);
                    totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                }
                double disc_per = Double.parseDouble(discText);
                System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
                Double amount = (totalTaxableAmt * disc_per) / 100;
                purchase_discount_amt.setText(String.valueOf(amount));
                PurchaseInvoiceCalculation.discountPropotionalCalculation(disc_per + "", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                if (!tfAddCharges.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                }
            } else {
                purchase_discount_amt.setText("");
                PurchaseInvoiceCalculation.discountPropotionalCalculation("0", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                if (!tfAddCharges.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                }
            }

            //? Calculate Tax for each Row
            PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);


        }
        invoiceProductHistoryTable();

        btnModify.setOnAction(actionEvent -> {
            PurchaseInvoiceListController.focusStatus="cancel";
            System.out.println(" selectedRowIndex in CreateModify " + selectedRowIndex + " focusStatus " + "cancel");
            if(batchIdToDelete!=null &&  !batchIdToDelete.equals("") ){
                deleteProductBatch(batchIdToDelete);
            }
            GlobalController.getInstance().addTabStatic(PURCHAE_INV_LIST_SLUG, false);
        });

        chbPurInvRoundOff.setOnAction(e -> {
            roundOffCalculations();
        });
        cmbPaymentMode.getSelectionModel().select("Credit");
        cmbPaymentMode.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    cmbPaymentMode.show();
                }
            }
        });
        if (purchase_id > 0) {
            String paymentMode = edit_response_map.get("paymentMode").toString();
            if (paymentMode.equalsIgnoreCase("cash")) {
                cmbPaymentMode.getSelectionModel().select("Cash");
            }
            if (paymentMode.equalsIgnoreCase("credit")) {
                cmbPaymentMode.getSelectionModel().select("Credit");
            }


        }
        tfInvoiceValidatition();
        tfInvoiceDateValidation();

        tfAddCharges.setEditable(false);
        tfAddCharges.setFocusTraversable(false);

        chbPurInvRoundOff.setOnAction(e -> {
            roundOffCalculations();
        });

        tvPurchaseInvoiceTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int selectedIndex = newValue.intValue();
                if (selectedIndex >= 0) {


                    String p_id = tvPurchaseInvoiceTable.getItems().get(selectedIndex).getProduct_id();



                    List<BatchWindowTableDTO> batchWindowTableDTOList = tvPurchaseInvoiceTable.getItems().get(selectedIndex).getBatchWindowTableDTOList();

                    String b_id = "";
                    if (batchWindowTableDTOList != null && !batchWindowTableDTOList.isEmpty()) {
                         b_id = batchWindowTableDTOList.get(0).getB_details_id();
                    }

                    System.out.println("Selected row index: " + selectedIndex + " - p_id : "+p_id);

                    if(p_id.isEmpty()){
                        lblPurInvProductBrand.setText("");
                        lblPurInvProductGroup.setText("");
                        lblPurInvProductSubGroup.setText("");
                        lblPurInvProductCategory.setText("");
                        lblPurInvProductHsn.setText("");
                        lblPurInvProductTaxType.setText("");
                        lblPurInvProductTaxPer.setText("");
                        lblPurInvProductMarginPer.setText("");
                        lblPurInvProductCost.setText("");
                        lblPurInvProductShelfId.setText("");
                        lblPurInvProductMinStock.setText("");
                        lblPurInvProductMaxStock.setText("");
                    }
                    else {
                        tranxProductDetailsFun(p_id);
                    }




                    if(b_id.isEmpty()) {
                        lblPurInvBatchNo.setText("");
                        lblPurInvMfgDate.setText("");
                        lblPurInvExpDate.setText("");
                        lblPurInvBarcode.setText("");
                        lblPurInvPurRate.setText("");
                        lblPurInvCost.setText("");
                        lblPurInvCostWithTax.setText("");
                        lblPurInvMrp.setText("");
                        lblPurInvFsrmh.setText("");
                        lblPurInvFssai.setText("");
                        lbPurInvBatchCsrmh.setText("");
                        lbPurInvBatchCsrai.setText("");
                        lblPurInvFreeqty.setText("");
                        lblPurInvMarginPer.setText("");
                    }
                    else {
                        tranxBatchDetailsFun(b_id);
                    }

                }
            }
        });
    }


    public void setPurChallDataToProduct() {

        String tranxDate = Communicator.text_to_date.fromString(tfTranxDate.getText()).toString();
        String ledgrId = ledger_id;
        String gstNum = cbSupplierGSTIN.getId();
        String purSerNum = tfPurchaseSerial.getText();
        String challanNo = tfInvoiceNo.getText();
        String purAcc = purchaseAcID;
                //cbPurchaseAc.getValue().getId();
        System.out.println( " Hello Purchase Account id >> "+ purAcc);

        String challanDate = Communicator.text_to_date.fromString(tfInvoiceDate.getText()).toString();
        System.out.println("purchase_id"+purchase_id);

        PurTranxToProductRedirectionDTO mData = new PurTranxToProductRedirectionDTO();
        mData.setRedirect(FxmFileConstants.PURCHASE_INV_CREATE_SLUG);
        mData.setTranxDate(tranxDate);
        mData.setLedegrId(ledgrId);
        mData.setGstNum(gstNum);
        mData.setPurSerNum(purSerNum);
        mData.setChallanNo(challanNo);
        mData.setPurAcc(purAcc);
        mData.setChallanDate(challanDate);
        mData.setLedgerRed(isLedgerRed);
        mData.setProductRed(isProductRed);
        mData.setTranxType("purchase");
        mData.setPurChallEditId(purchase_id);
        mData.setRedirection(true);

        mData.setRediPrdCurrIndex(rediCurrIndex);
        List<PurchaseInvoiceTable> currentItems = new ArrayList<>(tvPurchaseInvoiceTable.getItems());
        List<PurchaseInvoiceTable> list = new ArrayList<>(currentItems);

        mData.setRowData(list);

//        for (PurchaseInvoiceTable rowData : tblvPurChallCmpTRow.getItems()) {
//            PurchaseInvoiceTable mData1 = new PurchaseInvoiceTable();
//            mData1.setProduct_id(rowData.getProduct_id());
////            mData1.set
//        }
        if(isProductRed ==true){
            GlobalController.getInstance().addTabStaticWithIsNewTab("product-create", false, mData);
        }
        if(isLedgerRed == true){
            GlobalController.getInstance().addTabStaticWithIsNewTab("ledger-create", false, mData);
        }


    }


    public void setRedirectData(Object inObj) {
        PurTranxToProductRedirectionDTO franchiseDTO = (PurTranxToProductRedirectionDTO) inObj;

        Integer redirectIndex = franchiseDTO.getRediPrdCurrIndex();

        purPrdRedId=franchiseDTO.getPurChallEditId();

        System.out.println("purchase_id----->"+purPrdRedId);
        tfInvoiceNo.setText(franchiseDTO.getChallanNo());
        ledger_id=franchiseDTO.getLedegrId();
        tfPurchaseSerial.setText(franchiseDTO.getPurSerNum());
        tfLedgerName.setText(ledgersById(Long.valueOf(ledger_id)));
        int cnt=0,index=0;
        tvPurchaseInvoiceTable.getItems().clear();
        PurInvoiceCommunicator.levelAForPurInvoiceObservableList.clear();
      // purchase_id = PurchaseInvoiceListController.getPurchaseId();
        isRedirect=franchiseDTO.getRedirection();
        System.out.println("purchase  id : " + purchase_id+franchiseDTO.getPurAcc());
       // cbPurchaseAc.setId(franchiseDTO.getPurAcc());
        purchaseAcID=franchiseDTO.getPurAcc();

        if (purPrdRedId > 0) {
            btnSubmit.setText("Update");
            btnModify.setVisible(false);
        }

        System.out.println(" Called >> " + supplierGSTINList.size());
        System.out.println(" Called ledger_id >> " + ledger_id);
        tfLedgerName.setText(ledgersById(Long.valueOf(ledger_id)));
        if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
            cbSupplierGSTIN.setItems(supplierGSTINList);
            cbSupplierGSTIN.setValue(supplierGSTINList.get(0));
        }  System.out.println(" Called >> " + supplierGSTINList.size());
        System.out.println(" Called ledger_id >> " + ledger_id);
        tfLedgerName.setText(ledgersById(Long.valueOf(ledger_id)));
        if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
            cbSupplierGSTIN.setItems(supplierGSTINList);
            cbSupplierGSTIN.setValue(supplierGSTINList.get(0));
        }
        if(franchiseDTO.getProductRed()==true) {
            Long productId = Long.valueOf(franchiseDTO.getRedirectProductId());
            int rindex = franchiseDTO.getRediPrdCurrIndex();
            JsonObject obj = productRedirectById(productId);

            for (PurchaseInvoiceTable purchaseInvoiceTable : franchiseDTO.getRowData()) {
                if(index==rindex){
                    purchaseInvoiceTable.setProduct_id(productId+"");
                    purchaseInvoiceTable.setLedger_id(ledger_id);
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
                    purchaseInvoiceTable.setB_expiry("");
                    purchaseInvoiceTable.setManufacturing_date("");
                    BatchWindowTableDTO batchWindowTableDTO = new BatchWindowTableDTO();

                    batchWindowTableDTO.setProductId(purchaseInvoiceTable.getProduct_id());
                    batchWindowTableDTO.setCsr_mh(purchaseInvoiceTable.getRate_c());
                    batchWindowTableDTO.setCsr_ai(purchaseInvoiceTable.getRate_d().equals("0.0") ? "" : purchaseInvoiceTable.getRate_d());
                    batchWindowTableDTO.setFsr_mh(purchaseInvoiceTable.getRate_a());
                    batchWindowTableDTO.setFsr_ai(purchaseInvoiceTable.getRate_b());
                    batchWindowTableDTO.setMrp(purchaseInvoiceTable.getB_rate());
                    System.out.println("iceTable.getLevelA_id()"+purchaseInvoiceTable.getLevelA_id());

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
//            mData1.setParticulars(rowData.getParticulars());
//            mData1.setPackages(rowData.getPackages());
                tvPurchaseInvoiceTable.getItems().addAll(purchaseInvoiceTable);
                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(index, tvPurchaseInvoiceTable, callback);
                PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);
//                calculateGst(tableView, callback);

                List<LevelAForPurInvoice> levelAForPurInvoiceList = com.opethic.genivis.controller.tranx_purchase.ProductUnitsPacking.getAllProductUnitsPackingFlavour(purchaseInvoiceTable.getProduct_id());
                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
                if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurchaseInvoiceTable.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
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

        int inx=0;
        System.out.println("is ledgeriiiiiiiiiii"+franchiseDTO.getLedgerRed());
        // tblvPurChallCmpTRow.getItems().clear();
        if(franchiseDTO.getLedgerRed()==true) {
            System.out.println("is ledger"+franchiseDTO.getLedgerRed());
            for (PurchaseInvoiceTable rowData : franchiseDTO.getRowData()) {


                System.out.println("String.valueOf(rowData.getRowData().get(0))" + rowData.getProduct_id());
                System.out.println("String.valueOf(rowData.getRowData().get(0))" + rowData.getLedger_id());
//                mData1.setProduct_id(rowData.getProduct_id());
//                mData1.setParticulars(rowData.getParticulars());
//                mData1.setPackages(rowData.getPackages());


                List<LevelAForPurInvoice> levelAForPurInvoiceList = com.opethic.genivis.controller.tranx_purchase.ProductUnitsPacking.getAllProductUnitsPackingFlavour(rowData.getProduct_id());
                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);
                if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurchaseInvoiceTable.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }

                } else {
                    System.out.println("Index else >> : " + index);
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);
                }
                rowData.setLedger_id(ledger_id);
                tvPurchaseInvoiceTable.getItems().add(rowData);
                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(index, tvPurchaseInvoiceTable, callback);
                PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);

                inx++;
            }
        }
        Platform.runLater(()->{
            TableColumn<PurchaseInvoiceTable, ?> colName = tvPurchaseInvoiceTable.getColumns().get(1);
            tvPurchaseInvoiceTable.edit(redirectIndex, colName);
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

    private void resolution() {

        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);


        if (width == 1280) {

        } else if (width >= 1360 && width <= 1366) {

        } else if (width >= 1367 && width <= 1400) {

        } else if (width >= 1401 && width <= 1440) {

        } else if (width >= 1441 && width <= 1600) {

        } else if (width >= 1601 && width <= 1680) {

        } else if (width >= 1681 && width <= 1920) {

        }


        if (height == 1080) {
            r1080();
        } else if (height == 1050) {
            r1050();
        } else if (height == 1024) {
            r1024();
        } else if (height == 960) {
            r960();
        } else if (height == 900) {
            r900();
        } else if (height == 800) {
            r800();
        } else if (height == 768) {

        } else if (height == 720) {
        }


    }

    public void r1080() {

        main_bottom_hbox.setPrefHeight(375);
        main_bottom_hbox.setMinHeight(375);
        main_bottom_hbox.setMaxHeight(375);

        tvGST_Table.setPrefHeight(181);
        tvGST_Table.setMinHeight(181);
        tvGST_Table.setMaxHeight(181);


        amounts_bottom_section.setPrefHeight(260);
        amounts_bottom_section.setMinHeight(260);
        amounts_bottom_section.setMaxHeight(260);


        hbox_ledger_tab.setPrefHeight(130);
        hbox_ledger_tab.setMinHeight(130);
        hbox_ledger_tab.setMaxHeight(130);

        hbox_product_tab.setPrefHeight(130);
        hbox_product_tab.setMinHeight(130);
        hbox_product_tab.setMaxHeight(130);

        hbox_batch_tab.setPrefHeight(130);
        hbox_batch_tab.setMinHeight(130);
        hbox_batch_tab.setMaxHeight(130);


        vbox_header.setPrefHeight(100);
        vbox_header.setMinHeight(100);
        vbox_header.setMaxHeight(100);
        vbox_header.setSpacing(10);

        vbox_header.setPadding(new Insets(10, 10, 10, 10));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r1920.css").toExternalForm());
        setLabelSize(lbTranxDate, 73);
        setLabelSize(lbLedgerName, 89);
        setLabelSize(lbSuplierGSTN, 103);
        setLabelSize(lbPurchaseSerial, 106);
        setLabelSize(lbInvoiceNo, 75);
        setLabelSize(lbInvoiceDate, 83);
        setLabelSize(lbPurchaseAc, 92);
        setLabelSize(lbPaymentMethod, 114);
        setLabelSize(lbPurchaseOrder, 86);
        setLabelSize(lbPurchaseChallan, 91);
        tvPurchaseInvoiceTable.setMaxHeight(396);
        tvPurchaseInvoiceTable.setMinHeight(396);
        tvPurchaseInvoiceTable.setPrefHeight(396);
    }

    private void r1050() {


        main_bottom_hbox.setPrefHeight(375);
        main_bottom_hbox.setMinHeight(375);
        main_bottom_hbox.setMaxHeight(375);

        tvGST_Table.setPrefHeight(181);
        tvGST_Table.setMinHeight(181);
        tvGST_Table.setMaxHeight(181);


        amounts_bottom_section.setPrefHeight(260);
        amounts_bottom_section.setMinHeight(260);
        amounts_bottom_section.setMaxHeight(260);


        hbox_ledger_tab.setPrefHeight(130);
        hbox_ledger_tab.setMinHeight(130);
        hbox_ledger_tab.setMaxHeight(130);

        hbox_product_tab.setPrefHeight(130);
        hbox_product_tab.setMinHeight(130);
        hbox_product_tab.setMaxHeight(130);

        hbox_batch_tab.setPrefHeight(130);
        hbox_batch_tab.setMinHeight(130);
        hbox_batch_tab.setMaxHeight(130);


        vbox_header.setPrefHeight(100);
        vbox_header.setMinHeight(100);
        vbox_header.setMaxHeight(100);
        vbox_header.setSpacing(10);


        vbox_header.setPadding(new Insets(10, 10, 10, 10));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r1920.css").toExternalForm());
        tvPurchaseInvoiceTable.setMaxHeight(361);
        tvPurchaseInvoiceTable.setMinHeight(361);
        tvPurchaseInvoiceTable.setPrefHeight(361);
        setLabelSize(lbTranxDate, 73);
        setLabelSize(lbLedgerName, 89);
        setLabelSize(lbSuplierGSTN, 103);
        setLabelSize(lbPurchaseSerial, 106);
        setLabelSize(lbInvoiceNo, 75);
        setLabelSize(lbInvoiceDate, 83);
        setLabelSize(lbPurchaseAc, 92);
        setLabelSize(lbPaymentMethod, 114);
        setLabelSize(lbPurchaseOrder, 86);
        setLabelSize(lbPurchaseChallan, 91);
    }

    public void r1024() {

        amounts_bottom_section.setPrefHeight(230);
        amounts_bottom_section.setMinHeight(230);
        amounts_bottom_section.setMaxHeight(230);

        main_bottom_hbox.setPrefHeight(343);
        main_bottom_hbox.setMinHeight(343);
        main_bottom_hbox.setMaxHeight(343);

        tvGST_Table.setPrefHeight(181);
        tvGST_Table.setMinHeight(181);
        tvGST_Table.setMaxHeight(181);

        hbox_ledger_tab.setPrefHeight(130);
        hbox_ledger_tab.setMinHeight(130);
        hbox_ledger_tab.setMaxHeight(130);

        hbox_product_tab.setPrefHeight(130);
        hbox_product_tab.setMinHeight(130);
        hbox_product_tab.setMaxHeight(130);

        hbox_batch_tab.setPrefHeight(130);
        hbox_batch_tab.setMinHeight(130);
        hbox_batch_tab.setMaxHeight(130);

        vbox_header.setPrefHeight(95);
        vbox_header.setMinHeight(95);
        vbox_header.setMaxHeight(95);
        vbox_header.setSpacing(10);

        vbox_header.setPadding(new Insets(10, 10, 10, 10));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r1600.css").toExternalForm());
        tvPurchaseInvoiceTable.setMaxHeight(377);
        tvPurchaseInvoiceTable.setMinHeight(377);
        tvPurchaseInvoiceTable.setPrefHeight(377);
        setLabelSize(lbTranxDate, 63);
        setLabelSize(lbLedgerName, 77);
        setLabelSize(lbSuplierGSTN, 89);
        setLabelSize(lbPurchaseSerial, 92);
        setLabelSize(lbInvoiceNo, 62);
        setLabelSize(lbInvoiceDate, 72);
        setLabelSize(lbPurchaseAc, 82);
        setLabelSize(lbPaymentMethod, 100);
        setLabelSize(lbPurchaseOrder, 75);
        setLabelSize(lbPurchaseChallan, 80);
    }

    public void r960() {

        amounts_bottom_section.setPrefHeight(233);
        amounts_bottom_section.setMinHeight(233);
        amounts_bottom_section.setMaxHeight(233);

        main_bottom_hbox.setPrefHeight(350);
        main_bottom_hbox.setMinHeight(350);
        main_bottom_hbox.setMaxHeight(350);

        tvGST_Table.setPrefHeight(188);
        tvGST_Table.setMinHeight(188);
        tvGST_Table.setMaxHeight(188);

        hbox_ledger_tab.setPrefHeight(110);
        hbox_ledger_tab.setMinHeight(110);
        hbox_ledger_tab.setMaxHeight(110);

        hbox_product_tab.setPrefHeight(110);
        hbox_product_tab.setMinHeight(110);
        hbox_product_tab.setMaxHeight(110);

        hbox_batch_tab.setPrefHeight(110);
        hbox_batch_tab.setMinHeight(110);
        hbox_batch_tab.setMaxHeight(110);


        vbox_header.setPrefHeight(85);
        vbox_header.setMinHeight(85);
        vbox_header.setMaxHeight(85);
        vbox_header.setSpacing(7);

        vbox_header.setPadding(new Insets(8, 8, 8, 8));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r1600.css").toExternalForm());
        tvPurchaseInvoiceTable.setMaxHeight(315);
        tvPurchaseInvoiceTable.setMinHeight(315);
        tvPurchaseInvoiceTable.setPrefHeight(315);
        setLabelSize(lbTranxDate, 63);
        setLabelSize(lbLedgerName, 77);
        setLabelSize(lbSuplierGSTN, 89);
        setLabelSize(lbPurchaseSerial, 92);
        setLabelSize(lbInvoiceNo, 62);
        setLabelSize(lbInvoiceDate, 72);
        setLabelSize(lbPurchaseAc, 82);
        setLabelSize(lbPaymentMethod, 100);
        setLabelSize(lbPurchaseOrder, 75);
        setLabelSize(lbPurchaseChallan, 80);
    }

    public void r900() {

        amounts_bottom_section.setPrefHeight(180);
        amounts_bottom_section.setMinHeight(180);
        amounts_bottom_section.setMaxHeight(180);

        main_bottom_hbox.setPrefHeight(293);
        main_bottom_hbox.setMinHeight(293);
        main_bottom_hbox.setMaxHeight(293);

        tvGST_Table.setPrefHeight(157);
        tvGST_Table.setMinHeight(157);
        tvGST_Table.setMaxHeight(157);

        hbox_ledger_tab.setPrefHeight(97);
        hbox_ledger_tab.setMinHeight(97);
        hbox_ledger_tab.setMaxHeight(97);

        hbox_product_tab.setPrefHeight(97);
        hbox_product_tab.setMinHeight(97);
        hbox_product_tab.setMaxHeight(97);

        hbox_batch_tab.setPrefHeight(97);
        hbox_batch_tab.setMinHeight(97);
        hbox_batch_tab.setMaxHeight(97);


        vbox_header.setPrefHeight(85);
        vbox_header.setMinHeight(85);
        vbox_header.setMaxHeight(85);
        vbox_header.setSpacing(7);

        vbox_header.setPadding(new Insets(8, 8, 8, 8));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r1600.css").toExternalForm());
        tvPurchaseInvoiceTable.setMaxHeight(315);
        tvPurchaseInvoiceTable.setMinHeight(315);
        tvPurchaseInvoiceTable.setPrefHeight(315);
        setLabelSize(lbTranxDate, 63);
        setLabelSize(lbLedgerName, 77);
        setLabelSize(lbSuplierGSTN, 89);
        setLabelSize(lbPurchaseSerial, 92);
        setLabelSize(lbInvoiceNo, 62);
        setLabelSize(lbInvoiceDate, 72);
        setLabelSize(lbPurchaseAc, 82);
        setLabelSize(lbPaymentMethod, 100);
        setLabelSize(lbPurchaseOrder, 75);
        setLabelSize(lbPurchaseChallan, 80);
    }

    public void r800() {

        amounts_bottom_section.setPrefHeight(180);
        amounts_bottom_section.setMinHeight(180);
        amounts_bottom_section.setMaxHeight(180);

        main_bottom_hbox.setPrefHeight(280);
        main_bottom_hbox.setMinHeight(280);
        main_bottom_hbox.setMaxHeight(280);

        tvGST_Table.setPrefHeight(157);
        tvGST_Table.setMinHeight(157);
        tvGST_Table.setMaxHeight(157);

        hbox_ledger_tab.setPrefHeight(97);
        hbox_ledger_tab.setMinHeight(97);
        hbox_ledger_tab.setMaxHeight(97);

        hbox_product_tab.setPrefHeight(97);
        hbox_product_tab.setMinHeight(97);
        hbox_product_tab.setMaxHeight(97);

        hbox_batch_tab.setPrefHeight(97);
        hbox_batch_tab.setMinHeight(97);
        hbox_batch_tab.setMaxHeight(97);


        vbox_header.setPrefHeight(85);
        vbox_header.setMinHeight(85);
        vbox_header.setMaxHeight(85);
        vbox_header.setSpacing(7);

        vbox_header.setPadding(new Insets(8, 8, 8, 8));

        purScrollPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/purchase_invoice/r800.css").toExternalForm());
        tvPurchaseInvoiceTable.setMaxHeight(224);
        tvPurchaseInvoiceTable.setMinHeight(224);
        tvPurchaseInvoiceTable.setPrefHeight(224);
        setLabelSize(lbTranxDate, 63);
        setLabelSize(lbLedgerName, 77);
        setLabelSize(lbSuplierGSTN, 89);
        setLabelSize(lbPurchaseSerial, 92);
        setLabelSize(lbInvoiceNo, 62);
        setLabelSize(lbInvoiceDate, 72);
        setLabelSize(lbPurchaseAc, 82);
        setLabelSize(lbPaymentMethod, 100);
        setLabelSize(lbPurchaseOrder, 75);
        setLabelSize(lbPurchaseChallan, 80);
    }


    public void setLabelSize(Label label, double size) {
        label.setMinWidth(size);
        label.setMaxWidth(size);
        label.setPrefWidth(size);
    }

    private void tfInvoiceDateValidation() {
        tfInvoiceDate.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfInvoiceDate.getText().isEmpty()) {
                    tfInvoiceDate.requestFocus();
                } else {
                    validateInvoiceDate();
                }
            }
        });
    }

    private void tfLededgerNameEvent() {
        tfLedgerName.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                handleTfLedgerName();
            }
        });

        tfLedgerName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfLedgerName.getText().isEmpty()) {
                    tfLedgerName.requestFocus();
                }
            }
        });


        tfLedgerName.setOnMouseClicked(event -> {
            handleTfLedgerName();
        });
        tfLedgerName.setOnKeyTyped(event->{
            tfLedgerName.setText("");
            handleTfLedgerName();
        });
    }

    private void tfInvoiceValidatition() {
        tfInvoiceNo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (tfInvoiceNo.getText().isEmpty()) {
                    tfInvoiceNo.requestFocus();
                } else {
                    String btText = btnSubmit.getText();
                    if (btText.equalsIgnoreCase("Submit")) {
                        validatePurchaseInvoiceNo();
                    } else {
                        validatePurchaseInvoiceUpdate();
                    }
                }
            }
        });
    }

    private void setGSTFooterPart() {
        tvGST_Table.setFocusTraversable(false);
        tc_gst.setSortable(false);
        tc_cgst.setSortable(false);
        tc_sgst.setSortable(false);

        tc_gst.setCellValueFactory(cellData -> {
            StringProperty taxProperty = cellData.getValue().taxPerProperty();
            return Bindings.createStringBinding(

                    () -> "(" + (int) Double.parseDouble(!taxProperty.get().isEmpty() ? taxProperty.get() : "0.0") + " %)", taxProperty);
        });

        tc_gst.setStyle("-fx-alignment: CENTER;");
        tc_cgst.setCellValueFactory(cellData -> cellData.getValue().cgstProperty());
        tc_cgst.setCellFactory(col -> new TableCell<GstDTO, String>() {
            private final Label label = new Label();
            private final VBox vBox = new VBox(label);

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


        tc_cgst.setStyle("-fx-alignment: CENTER;");
        tc_sgst.setCellValueFactory(cellData -> cellData.getValue().sgstProperty());
        tc_sgst.setStyle("-fx-alignment: CENTER;");

        tc_sgst.setCellFactory(col -> new TableCell<GstDTO, String>() {
            private final Label label = new Label();
            private final VBox vBox = new VBox(label);

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
        //igst
        tc_igst.setCellValueFactory(cellData -> cellData.getValue().igstProperty());
        tc_igst.setCellFactory(col -> new TableCell<GstDTO, String>() {
            private final Label label = new Label();
            private final VBox vBox = new VBox(label);

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


        tc_igst.setStyle("-fx-alignment: CENTER;");

        tvGST_Table.getItems().addAll(new GstDTO("", "", "", ""));

    }

    private void getPaymentModes() {
        cmbPaymentMode.setItems(payment_mode_list);
    }

    private void initialEnterMethod() {
        purScrollPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("modify")) {
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("submit")) {
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("cancel")) {
                } else if (event.getTarget() instanceof Button targetButton && targetButton.getText().equalsIgnoreCase("update")) {
                } else {
                    KeyEvent newEvent = new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "\t", KeyCode.TAB, event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown());
                    Event.fireEvent(event.getTarget(), newEvent);
                    event.consume();
                }
            } else if (event.isControlDown() && event.getCode() == KeyCode.S) {
                String msg = "Do you want to create ";
                if (purchase_id > 0) msg = "Do you want to update ";
                AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, msg + tfInvoiceNo.getText() + " Invoice Number?", input -> {
                    if (input == 1) {
                        invoiceValidations();
                    }
                });
            }else if (event.isControlDown() && event.getCode() == KeyCode.E) {
                String msg = "Do you Really want to go list ";
                if (purchase_id < 0) {
                    AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, msg, input -> {
                        if (input == 1) {
                            GlobalController.getInstance().addTabStatic(PURCHAE_INV_LIST_SLUG, false);
                        }
                    });
                }
            }
        });

    }




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

    /**
     * Info :  Set the SUpplier Details of the Selected Product Bottom in Supplier Table
     *
     * @Author : Ashwin Shendre
     */

    private void getSupplierListbyProductId(String id) {
        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("productId", id);
            String formData = Globals.mapToStringforFormData(map);
            logger.debug("Form Data for getSupplierListbyProductId in Purchase Invoice Controller:" + formData);
            logger.debug("Network call started getSupplierListbyProductId() in Purchase Invoice Controller:");
            apiClient = new APIClient(EndPoints.GET_SUPPLIERLIST_BY_PRODUCTID_ENDPOINT, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray dataArray = jsonObject.getAsJsonArray("data");
                        System.out.println("dataArray >> : " + dataArray);
                        //todo: getting values
                        ObservableList<InvoiceProductHistoryTable> supplierDataList = FXCollections.observableArrayList();
                        for (JsonElement element : dataArray) {
                            JsonObject item = element.getAsJsonObject();
                            String supplierName = item.get("supplier_name").getAsString();
                            String supplierInvNo = item.get("invoice_no").getAsString();
                            String supplierInvDate = item.get("invoice_date").getAsString();
                            String supplierBatch = item.get("batch").getAsString();
                            String supplierMrp = item.get("mrp").getAsString();
                            String supplierQty = decimalFormat.format(item.get("quantity").getAsDouble());
                            String supplierRate = item.get("rate").getAsString();
                            String supplierCost = item.get("cost").getAsString();
                            String supplierDisPer = item.get("dis_per").getAsString();
                            String supplierDisAmt = item.get("dis_amt").getAsString();
                            supplierDataList.add(new InvoiceProductHistoryTable(supplierName, supplierInvNo, supplierInvDate, supplierBatch, supplierMrp, supplierQty, supplierRate, supplierCost, supplierDisPer, supplierDisAmt));
                        }
                        tvInvoiceProductHistory.setItems(supplierDataList);

                        logger.debug("Network call Successed getSupplierListbyProductId() in Purchase Invoice Controller:");
                    }

                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API cancelled in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API failed in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }
        logger.debug("tranxBatchDetailsFun() Ended");
    }

    public String ledgersById(Long id) {

        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(id));

        String formData = mapToStringforFormData(map);
        HttpResponse<String> response = APIClient.postFormDataRequest(formData, "get_ledgers_by_id");

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
                supplierGSTINList.add(new CommonDTO(gstin, gst_id));
            }
        }
        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
            taxFlag = true;
        }

        return ledgerName;
    }

    private void getPurchaseInvoiceByIdNew(Integer purchase_id) {

        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(purchase_id));
            String formData = mapToStringforFormData(map);

            HttpResponse<String> response = APIClient.postFormDataRequest(formData, "get_purchase_invoice_by_id_new");
            PurchaseEditResponseDTO purchaseEditResponseDTO = new Gson().fromJson(response.body(), PurchaseEditResponseDTO.class);
            System.out.println("res update: " + purchaseEditResponseDTO);

            if (purchaseEditResponseDTO.getResponseStatus() == 200) {
                setEditResponse(purchaseEditResponseDTO);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEditResponse(PurchaseEditResponseDTO purchaseEditResponseDTO) {

        edit_response_map.put("tcs_mode", purchaseEditResponseDTO.getTcsMode());
        edit_response_map.put("tcs_amt", purchaseEditResponseDTO.getTcsAmt());
        edit_response_map.put("tcs_per", purchaseEditResponseDTO.getTcsPer());
        edit_response_map.put("narration", purchaseEditResponseDTO.getNarration());
        edit_response_map.put("paymentMode", purchaseEditResponseDTO.getPaymentMode());
        edit_response_map.put("discountLedgerId", purchaseEditResponseDTO.getDiscountLedgerId());
        edit_response_map.put("discountInAmt", purchaseEditResponseDTO.getDiscountInAmt());
        edit_response_map.put("discountInPer", purchaseEditResponseDTO.getDiscountInPer());
        edit_response_map.put("totalPurchaseDiscountAmt", purchaseEditResponseDTO.getTotalPurchaseDiscountAmt());
        edit_response_map.put("totalQty", purchaseEditResponseDTO.getTotalQty());
        edit_response_map.put("totalFreeQty", purchaseEditResponseDTO.getTotalFreeQty());
        edit_response_map.put("grossTotal", purchaseEditResponseDTO.getGrossTotal());
        edit_response_map.put("totalTax", purchaseEditResponseDTO.getTotalTax());
        edit_response_map.put("additionLedger1", purchaseEditResponseDTO.getAdditionLedger1());
        edit_response_map.put("additionLedgerAmt1", purchaseEditResponseDTO.getAdditionLedgerAmt1());
        edit_response_map.put("additionLedger2", purchaseEditResponseDTO.getAdditionLedger2());
        edit_response_map.put("additionLedgerAmt2", purchaseEditResponseDTO.getAdditionLedgerAmt2());
        edit_response_map.put("additionLedger3", purchaseEditResponseDTO.getAdditionLedger3());
        edit_response_map.put("additionLedgerAmt3", purchaseEditResponseDTO.getAdditionLedgerAmt3());
        edit_response_map.put("debitNoteReference", purchaseEditResponseDTO.getDebitNoteReference());

        rowListDTOS.addAll(purchaseEditResponseDTO.getRow());
        addChargesResLst.addAll(purchaseEditResponseDTO.getAdditionalCharges());

        edit_response_map.put("invoiceData", purchaseEditResponseDTO.getInvoiceData());
        InvoiceDataResDTO invoiceDataResDTO = purchaseEditResponseDTO.getInvoiceData();
        invoice_data_map.put("id", String.valueOf(invoiceDataResDTO.getId()));
        invoice_data_map.put("invoice_dt", String.valueOf(invoiceDataResDTO.getInvoiceDt()));
        invoice_data_map.put("invoice_no", String.valueOf(invoiceDataResDTO.getInvoiceNo()));
        invoice_data_map.put("tranx_unique_code", String.valueOf(invoiceDataResDTO.getTranxUniqueCode()));
        invoice_data_map.put("purchase_sr_no", String.valueOf(invoiceDataResDTO.getPurchaseSrNo()));
        invoice_data_map.put("purchase_account_ledger_id", String.valueOf(invoiceDataResDTO.getPurchaseAccountLedgerId()));
        invoice_data_map.put("supplierId", String.valueOf(invoiceDataResDTO.getSupplierId()));
        ledger_id = invoice_data_map.get("supplierId");
        purchaseAcID=invoice_data_map.get("purchase_account_ledger_id");

        tranxLedgerDetailsFun(ledger_id);
        invoice_data_map.put("transaction_dt", String.valueOf(invoiceDataResDTO.getTransactionDt()));
        invoice_data_map.put("additional_charges_total", String.valueOf(invoiceDataResDTO.getAdditionalChargesTotal()));
        invoice_data_map.put("gstNo", String.valueOf(invoiceDataResDTO.getGstNo()));
        invoice_data_map.put("isRoundOffCheck", String.valueOf(invoiceDataResDTO.getIsRoundOffCheck()));
        invoice_data_map.put("roundoff", String.valueOf(invoiceDataResDTO.getRoundoff()));
        invoice_data_map.put("mode", String.valueOf(invoiceDataResDTO.getMode()));
        invoice_data_map.put("image", String.valueOf(invoiceDataResDTO.getImage()));


        edit_response_map.put("bills", purchaseEditResponseDTO.getBills());
        edit_response_map.put("barcodeList", purchaseEditResponseDTO.getBarcodeList());
        edit_response_map.put("additionalCharges", purchaseEditResponseDTO.getAdditionalCharges());

    }

    private void setAddChargesInTable() {
        for (AddChargesListDTO addChgLst : addChargesResLst) {
            CustomAddChargesDTO customAddChargesDTO = new CustomAddChargesDTO();
            customAddChargesDTO.setAdditional_charges_details_id(String.valueOf(addChgLst.getAdditionalChargesDetailsId()));
            customAddChargesDTO.setLedgerId(String.valueOf(addChgLst.getLedgerId()));
            customAddChargesDTO.setAmt(String.valueOf(addChgLst.getAmt()));
            customAddChargesDTO.setPercent(String.valueOf(addChgLst.getPercent()));
            addChargesDTOList.add(customAddChargesDTO);
        }
    }

    private void invoiceProductHistoryTable() {

        tvInvoiceProductHistory.setFocusTraversable(false);
        cmSupplierName.setCellValueFactory(cellData -> cellData.getValue().supplier_nameProperty());
        cmSupplierName.setStyle("-fx-alignment: CENTER;");
        cmInvNo.setCellValueFactory(cellData -> cellData.getValue().invoice_noProperty());
        cmInvNo.setStyle("-fx-alignment: CENTER;");
        cmInvDate.setCellValueFactory(cellData -> cellData.getValue().invoice_dateProperty());
        cmInvDate.setStyle("-fx-alignment: CENTER;");
        cmBatch.setCellValueFactory(cellData -> cellData.getValue().batchProperty());
        cmBatch.setStyle("-fx-alignment: CENTER;");
        cmMRP.setCellValueFactory(cellData -> cellData.getValue().mrpProperty());
        cmMRP.setStyle("-fx-alignment: CENTER;");
        cmQty.setCellValueFactory(cellData -> cellData.getValue().qtyProperty());
        cmQty.setStyle("-fx-alignment: CENTER;");
        cmRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty());
        cmRate.setStyle("-fx-alignment: CENTER;");
        cmCost.setCellValueFactory(cellData -> cellData.getValue().costProperty());
        cmCost.setStyle("-fx-alignment: CENTER;");
        cmDisPer.setCellValueFactory(cellData -> cellData.getValue().dis_perProperty());
        cmDisPer.setStyle("-fx-alignment: CENTER;");
        cmDisAmt.setCellValueFactory(cellData -> cellData.getValue().dis_amtProperty());
        cmDisAmt.setStyle("-fx-alignment: CENTER;");
    }

    public void sceneInitilization() {
        purScrollPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof Stage) {
                Communicator.stage = (Stage) newScene.getWindow();
            }
        });
    }

    public void handlePayAction(ActionEvent actionEvent) {
        RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedRadioButton != null) {
            selectedPayMode = selectedRadioButton.getText();
        }
    }


    public void handleTfLedgerName() {
        Stage stage = (Stage) purScrollPane.getScene().getWindow();
        SingleInputDialogs.openLedgerPopUpForPurchaseInvoice(stage,ledger_id, "Creditors", input -> {
            supplierGSTINList.clear();
            System.out.println("input ledger selection =>" + input[3]);
            System.out.println("ledger selection id =>" + input[1]);

            purc_orders.setText((String) input[8]);
            purc_challan.setText((String) input[9]);
            tfLedgerName.setText(input[0].toString());
            ledger_id = (String) input[1];
            ledgerStateCode = (String) input[3];
            tvPurchaseInvoiceTable.getItems().get(0).setLedger_id(ledger_id);
            @SuppressWarnings("unchecked") ObservableList<GstDetailsDTO> gstDetailsDTOS = (ObservableList<GstDetailsDTO>) input[2];

            for (GstDetailsDTO gstDetailsDTO : gstDetailsDTOS) {
                supplierGSTINList.add(new CommonDTO(gstDetailsDTO.getGstNo(), gstDetailsDTO.getId()));
            }
            System.out.println(" supplierGSTINList >> : " + supplierGSTINList.size());
            if (supplierGSTINList != null && !supplierGSTINList.isEmpty()) {
                cbSupplierGSTIN.setItems(supplierGSTINList);
                cbSupplierGSTIN.setValue(supplierGSTINList.get(0));
                if (supplierGSTINList.size() > 1) {
                    cbSupplierGSTIN.requestFocus();
                } else {
                    tfInvoiceNo.requestFocus();
                }
            } else {
                tfInvoiceNo.requestFocus();
            }
            if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
                taxFlag = true;
            }
            tranxLedgerDetailsFun(ledger_id);
        },in->{
            System.out.println("In >> Called >. "+ in);
            if(in == true){
                isLedgerRed = true;
                setPurChallDataToProduct();
            }
        });
    }

    private void tranxLedgerDetailsFun(String id) {
        tranxPurInvTabPane.getSelectionModel().select(tabPurInvLedger);

        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("ledger_id", id);
            String formData = Globals.mapToStringforFormData(map);
            apiClient = new APIClient(EndPoints.TRANSACTION_LEDGER_DETAILS, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonObject jsonObj = jsonObject.getAsJsonObject("result");
                        CompanyStateCode = jsonObject.get("company_state_code").getAsLong();
                        stateCode = jsonObj.get("stateCode").getAsLong();

                        lblPurInvGstNo.setText(jsonObj.get("gst_number").getAsString());
                        lblPurInvArea.setText(jsonObj.get("area").getAsString());
                        lblPurInvBank.setText(jsonObj.get("bank_name").getAsString());
                        lblPurInvContactPerson.setText(jsonObj.get("contact_name").getAsString());
                        lblPurInvCreditDays.setText(jsonObj.get("credit_days").getAsString());
                        lblPurInvFssai.setText(jsonObj.get("fssai_number").getAsString());
                        lblPurInvLicenseNo.setText(jsonObj.get("license_number").getAsString());
                        lblPurInvRoute.setText(jsonObj.get("route").getAsString());
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API cancelled in tranxLedgerDetailsFun() Pur Invoice" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API failed in tranxLedgerDetailsFun() Pur Invoice" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }

    }


    private void nodetraversal(Node current_node, Node next_node) {
        current_node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                next_node.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                if (current_node instanceof TextField textField) {
                    if (textField.getId().equals("tfLedgerName")) {
                        handleTfLedgerName();
                    }
                }

                if (current_node instanceof RadioButton radioButton) {
                    radioButton.setSelected(!radioButton.isSelected());
                    radioButton.fire();
                }
            }
        });
    }

    public void getPurchaseAc() {

        try {

            HttpResponse<String> response = APIClient.getRequest("get_purchase_accounts");
            PurchaseAcRes purchaseAcRes = new Gson().fromJson(response.body(), PurchaseAcRes.class);

            if (purchaseAcRes.getResponseStatus() == 200) {

                List<PurchaseAcList> purchaseAcResLists = purchaseAcRes.getList();

                for (PurchaseAcList purchaseAcList1 : purchaseAcResLists) {
                    purchaseAcList.add(new CommonDTO(purchaseAcList1.getName(), purchaseAcList1.getId().toString()));
                }
                cbPurchaseAc.setItems(purchaseAcList);
                AutoCompleteBox<CommonDTO> autoCompleteBox = new AutoCompleteBox<>(cbPurchaseAc, -1);

            } else {
                System.out.println("responseObject is null");
            }
            cbPurchaseAc.getSelectionModel().selectFirst();
            System.out.println("Purchase Acc Id:" + cbPurchaseAc.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getPurchaseSerial() {

        try {
            HttpResponse<String> response = APIClient.getRequest("get_last_invoice_record");
            PurchaseSerialRes purchaseSerialRes = new Gson().fromJson(response.body(), PurchaseSerialRes.class);

            if (purchaseSerialRes.getResponseStatus() == 200) {
                tfPurchaseSerial.setText(String.valueOf(purchaseSerialRes.getCount()));
            } else {
                System.out.println("responseObject is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setCbPurchaseAc(ActionEvent actionEvent) {
        Object[] object = new Object[1];
        object[0] = cbPurchaseAc.getSelectionModel().getSelectedItem();
        System.out.println("Pur Acc :" + object[0].toString());
        if (object[0] != null) {
            for (CommonDTO commonDTO : purchaseAcList) {
                if (object[0].toString().equalsIgnoreCase(commonDTO.getText())) {
                    purchaseAcID = commonDTO.getId();
                }
            }
        }
        nodetraversal(cbSupplierGSTIN, tfInvoiceNo);
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

        tvGST_Table.getItems().clear();
        ObservableList<GstDTO> gstDTOObservableList = FXCollections.observableArrayList();
        ObservableList<?> item9List = (ObservableList<?>) item[9];
        for (Object obj : item9List) {
            if (obj instanceof GstDTO) {
                gstDTOObservableList.add((GstDTO) obj);
            }
        }

        tvGST_Table.getItems().addAll(gstDTOObservableList);

        if(!ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())){
            tc_igst.setVisible(true);
            tc_cgst.setVisible(false);
            tc_sgst.setVisible(false);
        }
        else{
            tc_igst.setVisible(false);
            tc_cgst.setVisible(true);
            tc_sgst.setVisible(true);
        }
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
        purchase_invoice_map.put("taxCalculation", jsonResult.toString());


        lbBillAmount.setText((String) item[2]); //totalNetAmt for bill amount
        totalamt = (String) item[2];
        lbtotalQty.setText((String) item[0]);
        lbtotalFreeQty.setText((String) item[1]);

        total_purchase_discount_amt = (String) item[4];

        System.out.println("ledgerStateCode =>" + ledgerStateCode + "GlobalTranx.getCompanyStateCode() =>" + GlobalTranx.getCompanyStateCode());


        if (ledgerStateCode.equalsIgnoreCase(GlobalTranx.getCompanyStateCode().toString())) {
            taxFlag = true;
        }

        purchase_invoice_map.put("total_qty", (String) item[0]);
        purchase_invoice_map.put("total_free_qty", (String) item[1]);

        purchase_invoice_map.put("total_base_amt", (String) item[3]);
        purchase_invoice_map.put("total_invoice_dis_amt", (String) item[4]);
        purchase_invoice_map.put("taxable_amount", (String) item[5]);
        purchase_invoice_map.put("bill_amount", (String) item[2]);
        purchase_invoice_map.put("total_tax_amt", (String) item[6]);

        purchase_invoice_map.put("totaligst", (String) item[6]);
        purchase_invoice_map.put("totalsgst", (String) item[7]);
        purchase_invoice_map.put("totalcgst", (String) item[8]);


        purchase_invoice_map.put("taxFlag", String.valueOf(taxFlag));

        total_taxable_amt = Double.parseDouble((String) item[5]);


        lbGrossTotal.setText((String) item[3]);
        lbDiscount.setText((String) item[4]);
        lbTotal.setText((String) item[5]);
        lbTax.setText((String) item[6]);
        chbPurInvRoundOff.setSelected(true);
        roundOffCalculations();
    };
    TableCellCallback<Object[]> delCallback = item -> {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("del_id", item[0].toString());
        deletedRows.add(jsonObject);
        if (deletedRows.size() > 0) {
//            Float dis_per_float = (Float) edit_response_map.get("discountInPer");
//            purchase_discount.setText(String.valueOf(dis_per_float));
            String discText = purchase_discount.getText();
            if (!TextUtils.isEmpty(discText)) {
                double totalTaxableAmt = 0.0;
                for (int i = 0; i < tvPurchaseInvoiceTable.getItems().size(); i++) {
                    PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(i, tvPurchaseInvoiceTable, callback);//call row calculation function
                    PurchaseInvoiceTable purchaseInvoiceTable = tvPurchaseInvoiceTable.getItems().get(i);
                    totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                }
                double disc_per = Double.parseDouble(discText);
                System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
                Double amount = (totalTaxableAmt * disc_per) / 100;
                purchase_discount_amt.setText(String.valueOf(amount));
                PurchaseInvoiceCalculation.discountPropotionalCalculation(disc_per + "", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                if (!tfAddCharges.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                }
            } else {
                purchase_discount_amt.setText("");
                PurchaseInvoiceCalculation.discountPropotionalCalculation("0", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                if (!tfAddCharges.getText().isEmpty()) {
                    System.out.println("Additioanal CHarges Called >> ");
                    PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                }
            }

            //? Calculate Tax for each Row
            PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);


        }

    };
    TableCellCallback<Object[]> productID_callback = item -> {
        if (item.length == 1) {
            System.out.println("tranxProductDetailsFun() started" + (String) item[0]);
            tranxProductDetailsFun((String) item[0]);
            logger.debug("tranxBatchDetailsFun() started");
            getSupplierListbyProductId((String) item[0]);
            if(tvPurchaseInvoiceTable.getSelectionModel().getFocusedIndex() != 0) {
                lblPurInvProductBrand.setText("");
                lblPurInvProductGroup.setText("");
                lblPurInvProductSubGroup.setText("");
                lblPurInvProductCategory.setText("");
                lblPurInvProductHsn.setText("");
                lblPurInvProductTaxType.setText("");
                lblPurInvProductTaxPer.setText("");
                lblPurInvProductMarginPer.setText("");
                lblPurInvProductCost.setText("");
                lblPurInvProductShelfId.setText("");
                lblPurInvProductMinStock.setText("");
                lblPurInvProductMaxStock.setText("");
            }
        } else {
            logger.debug("tranxBatchDetailsFun() started");
            System.out.println("tranxBatchDetailsFun() started batchNo " + (String) item[0] + " batchId " +  (String) item[0]);
            tranxBatchDetailsFun((String) item[1]);
            batchIdToDelete = Integer.valueOf((String) item[1]);
            System.out.println("created Batch Id is " + batchIdToDelete);
            String fieldCheck = (String) item[1];
            if(!fieldCheck.isEmpty()){
                String discText = purchase_discount.getText();
                if (!TextUtils.isEmpty(discText)) {
                    double totalTaxableAmt = 0.0;
                    for (int i = 0; i < tvPurchaseInvoiceTable.getItems().size(); i++) {
                        PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(i, tvPurchaseInvoiceTable, callback);//call row calculation function
                        PurchaseInvoiceTable purchaseInvoiceTable = tvPurchaseInvoiceTable.getItems().get(i);
                        totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
                    }
                    double disc_per = Double.parseDouble(discText);
                    System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
                    Double amount = (totalTaxableAmt * disc_per) / 100;
                    purchase_discount_amt.setText(String.valueOf(amount));
                    PurchaseInvoiceCalculation.discountPropotionalCalculation(disc_per + "", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                    if (!tfAddCharges.getText().isEmpty()) {
                        System.out.println("Additioanal CHarges Called >> ");
                        PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                    }
                } else {
                    purchase_discount_amt.setText("");
                    PurchaseInvoiceCalculation.discountPropotionalCalculation("0", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                    if (!tfAddCharges.getText().isEmpty()) {
                        System.out.println("Additioanal CHarges Called >> ");
                        PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
                    }
                }

                //? Calculate Tax for each Row
                PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);
            }

        }
    };

    private void roundOffCalculations() {
        Double billamt = 0.00; // local variable to store row wise  Bill amount
        Double total_bill_amt = 0.00; // local variable to store Final bill Amount without roundoff
        Double final_r_off_bill_amt = 0.00; // local variable to store Final Bill amount with round off
        Double roundOffAmt = 0.00; // local variable to store Roundoff Amount
        for (PurchaseInvoiceTable cmpTRowDTO : tvPurchaseInvoiceTable.getItems()) {
            billamt = cmpTRowDTO.getNet_amount()!=null && !cmpTRowDTO.getNet_amount().isEmpty() ? Double.valueOf(cmpTRowDTO.getNet_amount()):0.0;
            total_bill_amt += billamt;
        }
        final_r_off_bill_amt = (double) Math.round(total_bill_amt);
        roundOffAmt = final_r_off_bill_amt - total_bill_amt;

        if (chbPurInvRoundOff.isSelected()) {
            lbBillAmount.setText(String.format("%.2f", final_r_off_bill_amt));
            lblPurInvRoundOff.setText(String.format("%.2f", roundOffAmt));
        } else {
            lbBillAmount.setText(String.format("%.2f", total_bill_amt));
            lblPurInvRoundOff.setText("0.00");
        }

    }


    public void tableInitiliazation() {

        tvPurchaseInvoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tvPurchaseInvoiceTable.setEditable(true);
        tvPurchaseInvoiceTable.setFocusTraversable(false);


        Label headerLabel = new Label("Sr\nNo.");
        tcSrNo.setGraphic(headerLabel);

        if (purchase_id > 0) {

            int count = 1;
            int index = 0;
            for (RowListDTO rowListDTO : rowListDTOS) {

                PurchaseInvoiceTable purchaseInvoiceTable = new PurchaseInvoiceTable();
                purchaseInvoiceTable.setPurchase_id(String.valueOf(purchase_id));
                tranxProductDetailsFun(String.valueOf(purchase_id));
                getSupplierListbyProductId(String.valueOf(purchase_id));
                purchaseInvoiceTable.setSr_no(String.valueOf(count));
                purchaseInvoiceTable.setDetails_id(String.valueOf(rowListDTO.getDetailsId()));
                purchaseInvoiceTable.setProduct_id(String.valueOf(rowListDTO.getProductId()));
                purchaseInvoiceTable.setParticulars(String.valueOf(rowListDTO.getProductName()));

                purchaseInvoiceTable.setLevelA_id(String.valueOf(rowListDTO.getLevelAId()));
                purchaseInvoiceTable.setLevelB_id(String.valueOf(rowListDTO.getLevelBId()));
                purchaseInvoiceTable.setLevelC_id(String.valueOf(rowListDTO.getLevelCId()));
                purchaseInvoiceTable.setUnit_id(String.valueOf(rowListDTO.getUnitId()));
                purchaseInvoiceTable.setUnit_conv(String.valueOf(rowListDTO.getUnitConv()));

                purchaseInvoiceTable.setPackages(String.valueOf(rowListDTO.getPacking()));

                purchaseInvoiceTable.setQuantity(decimalFormat.format(Double.valueOf(rowListDTO.getQty())));//Qty in integer
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
                purchaseInvoiceTable.setFree(decimalFormat.format(Double.valueOf(rowListDTO.getFreeQty())));
                purchaseInvoiceTable.setRow_dis_amt(String.valueOf(rowListDTO.getRowDisAmt()));

                purchaseInvoiceTable.setGross_amount(String.valueOf(rowListDTO.getGrossAmt()));
                purchaseInvoiceTable.setGross_amount1(String.valueOf(rowListDTO.getGrossAmt()));

                purchaseInvoiceTable.setAdd_chg_amt(String.valueOf(rowListDTO.getAddChgAmt()));


                purchaseInvoiceTable.setInvoice_dis_amt(String.valueOf(rowListDTO.getInvoiceDisAmt()));
                purchaseInvoiceTable.setInventoryId(String.valueOf(rowListDTO.getInventoryId()));

                purchaseInvoiceTable.setTransaction_status(String.valueOf(rowListDTO.getTransactionStatus()));

                purchaseInvoiceTable.setIs_expired(String.valueOf(rowListDTO.getIsExpired()));


                purchaseInvoiceTable.setB_details_id(String.valueOf(rowListDTO.getbDetailsId()));

                purchaseInvoiceTable.setB_no(String.valueOf(rowListDTO.getBatchNo()));
                tranxBatchDetailsFun(String.valueOf(rowListDTO.getbDetailsId()));
                purchaseInvoiceTable.setBatch_or_serial(String.valueOf(rowListDTO.getBatchNo()));
                purchaseInvoiceTable.setB_expiry(String.valueOf(rowListDTO.getbExpiry()));
                purchaseInvoiceTable.setB_rate(String.valueOf(rowListDTO.getbRate()));
                purchaseInvoiceTable.setB_purchase_rate(String.valueOf(rowListDTO.getPurchaseRate()));
                purchaseInvoiceTable.setIs_batch(String.valueOf(rowListDTO.getIsBatch()));

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
                LocalDate manu_dt = null;
//                if (!purchaseInvoiceTable.getManufacturing_date().isEmpty()) {
//                    manu_dt = LocalDate.parse(purchaseInvoiceTable.getManufacturing_date());
//                    batchWindowTableDTO.setManufacturingDate(manu_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                } else {
//                    batchWindowTableDTO.setManufacturingDate("");
//                }
//                LocalDate exp_dt = null;
//                if (purchaseInvoiceTable.getB_expiry().isEmpty()) {
//                    exp_dt = LocalDate.parse(purchaseInvoiceTable.getB_expiry());
//                    batchWindowTableDTO.setExpiryDate(exp_dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                } else {
//                    batchWindowTableDTO.setExpiryDate("");
//                }

                batchWindowTableDTO.setMrp(purchaseInvoiceTable.getB_rate());
                batchWindowTableDTO.setPurchaseRate(purchaseInvoiceTable.getB_purchase_rate());

                batchWindowTableDTO.setQuantity(decimalFormat.format(Double.valueOf(purchaseInvoiceTable.getQuantity())));
                batchWindowTableDTO.setFree(decimalFormat.format(Double.valueOf(purchaseInvoiceTable.getFree())));

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

                tvPurchaseInvoiceTable.getItems().addAll(purchaseInvoiceTable);
                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(index, tvPurchaseInvoiceTable, callback);

                PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);
                List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPacking.getAllProductUnitsPackingFlavour(purchaseInvoiceTable.getProduct_id());

                ObservableList<LevelAForPurInvoice> observableLevelAList = FXCollections.observableArrayList(levelAForPurInvoiceList);

                if (index >= 0 && index < PurInvoiceCommunicator.levelAForPurInvoiceObservableList.size()) {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.set(index, observableLevelAList);

                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurchaseInvoiceTable.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }

                } else {
                    PurInvoiceCommunicator.levelAForPurInvoiceObservableList.add(observableLevelAList);

                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                    tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoiceList.get(0).getLabel());

                    for (LevelAForPurInvoice levelAForPurInvoice : PurInvoiceCommunicator.levelAForPurInvoiceObservableList.get(index)) {
                        if (tvPurchaseInvoiceTable.getItems().get(index).getLevelA_id().equals(levelAForPurInvoice.getValue())) {
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(null);
                            tvPurchaseInvoiceTable.getItems().get(index).setLevelA(levelAForPurInvoice.getLabel());
                        }
                    }
                }

                count++;
                index++;
            }
            setAddChargesInTable();

            cmbPaymentMode.setOnKeyPressed(event -> {
                if (actionPerformed == 0 && event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
                    actionPerformed = 1;
                    System.out.println("tvPurchaseInvoiceTable : " + tvPurchaseInvoiceTable.getItems().size());
                    tvPurchaseInvoiceTable.getItems().addAll(new PurchaseInvoiceTable("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                    cmbPaymentMode.requestFocus();

                    PauseTransition pause = new PauseTransition(Duration.seconds(0.0001));
                    pause.setOnFinished(eve -> {
                        Platform.runLater(() -> {
                            tvPurchaseInvoiceTable.edit(tvPurchaseInvoiceTable.getItems().size() - 1, tvPurchaseInvoiceTable.getColumns().get(1));
                        });
                    });
                    pause.play();

                    event.consume();

                } else {
                    Platform.runLater(() -> {
                        tvPurchaseInvoiceTable.edit(tvPurchaseInvoiceTable.getItems().size() - 1, tvPurchaseInvoiceTable.getColumns().get(1));
                    });
                }
            });


        } else {
            tvPurchaseInvoiceTable.getItems().addAll(new PurchaseInvoiceTable("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        }


//        tcSrNo.setCellValueFactory(cellData -> cellData.getValue().sr_noProperty());
        tcSrNo.setCellFactory(new LineNumbersCellFactory());

        tcSrNo.setStyle("-fx-alignment: CENTER;");

        tcPackage.setCellValueFactory(cellData -> cellData.getValue().packagesProperty());
        tcPackage.setStyle("-fx-alignment: CENTER;");

        tcLevelB.setCellValueFactory(cellData -> cellData.getValue().levelBProperty());
        tcLevelB.setCellFactory(column -> new ComboBoxTableCellForLevelB("tcLevelB"));

        tcLevelA.setCellValueFactory(cellData -> cellData.getValue().levelAProperty());
        tcLevelA.setCellFactory(column -> new ComboBoxTableCellForLevelA("tcLevelA"));

        tcLevelC.setCellValueFactory(cellData -> cellData.getValue().levelCProperty());
        tcLevelC.setCellFactory(column -> new ComboBoxTableCellForLevelC("tcLevelC"));

        tcUnit.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        tcUnit.setCellFactory(column -> new ComboBoxTableCellForUnit("tcUnit"));

        tcParticulars.setCellValueFactory(cellData -> cellData.getValue().particularsProperty());
        tcParticulars.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcParticulars", callback, productID_callback, tfNarration,addPrdCalbak));


        tcBatch.setCellValueFactory(cellData -> cellData.getValue().batch_or_serialProperty());
        tcBatch.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcBatch", callback, productID_callback, tfNarration));
        tcQuantity.setStyle("-fx-alignment: CENTER-RIGHT;");

        tcQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
       //  tcQuantity.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcQuantity", callback));
        tcQuantity.setStyle("-fx-alignment: CENTER-RIGHT;");

        tcFreeQty.setCellValueFactory(cellData -> cellData.getValue().freeProperty());
         tcFreeQty.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcFreeQty", callback));
        tcFreeQty.setStyle("-fx-alignment: CENTER-RIGHT;");


        tcRate.setCellValueFactory(cellData -> cellData.getValue().rateProperty());
        // tcRate.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcRate", callback));
        tcRate.setStyle("-fx-alignment: CENTER-RIGHT;");

        tcGrossAmount.setCellValueFactory(cellData -> cellData.getValue().gross_amountProperty());
        //  tcGrossAmount.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcGrossAmount", callback));
        tcGrossAmount.setStyle("-fx-alignment: CENTER-RIGHT;");


        tcDisPer.setCellValueFactory(cellData -> cellData.getValue().dis_perProperty());
        //   tcDisPer.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcDisPer", callback));
        tcDisPer.setStyle("-fx-alignment: CENTER-RIGHT;");


        //   tcDisPer.setCellValueFactory(cellData -> cellData.getValue().dis_perProperty());
        //  tcDisPer.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcDisPer", callback));

        tcDisAmt.setCellValueFactory(cellData -> cellData.getValue().dis_amtProperty());
        //  tcDisAmt.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcDisAmt", callback));
        tcDisAmt.setStyle("-fx-alignment: CENTER-RIGHT;");


        tcTax.setCellValueFactory(cellData -> cellData.getValue().taxProperty());
        //   tcTax.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcTax", callback));
        tcTax.setStyle("-fx-alignment: CENTER-RIGHT;");


        tcNetAmount.setCellValueFactory(cellData -> cellData.getValue().net_amountProperty());
        //    tcNetAmount.setCellFactory(column -> new TextFieldTableCellForPurInvoice("tcNetAmount", callback));
        tcNetAmount.setStyle("-fx-alignment: CENTER-RIGHT;");


        //  tcAction.setCellValueFactory(cellData -> cellData.getValue().net_amountProperty());
        tcAction.setCellFactory(column -> new ButtonTableCellDelete(delCallback,tfNarration));


        columnVisibility(tcLevelA, Globals.getUserControlsWithSlug("is_level_a"));
        columnVisibility(tcLevelB, Globals.getUserControlsWithSlug("is_level_b"));
        columnVisibility(tcLevelC, Globals.getUserControlsWithSlug("is_level_c"));
        columnVisibility(tcFreeQty, Globals.getUserControlsWithSlug("is_free_qty"));

        tcLevelA.setText(Globals.getUserControlsNameWithSlug("is_level_a"));
        tcLevelB.setText(Globals.getUserControlsNameWithSlug("is_level_B"));
        tcLevelC.setText(Globals.getUserControlsNameWithSlug("is_level_C"));

    }

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

    private void tranxProductDetailsFun(String id) {
        tranxPurInvTabPane.getSelectionModel().select(tabPurInvProduct);

        APIClient apiClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("product_id", id);
            String formData = Globals.mapToStringforFormData(map);
            apiClient = new APIClient(EndPoints.TRANSACTION_PRODUCT_DETAILS, formData, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    if(jsonObject!=null){
                        if (jsonObject.get("responseStatus").getAsInt() == 200) {
                            JsonObject productJsonObj = jsonObject.getAsJsonObject("result");
                            lblPurInvProductBrand.setText(productJsonObj.get("brand").getAsString());
                            lblPurInvProductGroup.setText(productJsonObj.get("group").getAsString());
                            lblPurInvProductSubGroup.setText(productJsonObj.get("subgroup").getAsString());
                            lblPurInvProductCategory.setText(productJsonObj.get("category").getAsString());
                            lblPurInvProductHsn.setText(productJsonObj.get("hsn").getAsString());
                            lblPurInvProductTaxType.setText(productJsonObj.get("tax_type").getAsString());
                            lblPurInvProductTaxPer.setText(productJsonObj.get("tax_per").getAsString());
                            lblPurInvProductMarginPer.setText(productJsonObj.get("margin_per").getAsString());
                            lblPurInvProductCost.setText(productJsonObj.get("cost").getAsString());
                            lblPurInvProductShelfId.setText(productJsonObj.get("shelf_id").getAsString());
                            int min = (int) Double.parseDouble(productJsonObj.get("min_stocks").getAsString());
                            lblPurInvProductMinStock.setText(String.valueOf(min));
                            int max = (int) Double.parseDouble(productJsonObj.get("max_stocks").getAsString());
                            lblPurInvProductMaxStock.setText(String.valueOf(max));
                        }
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API cancelled in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    logger.error("Network API failed in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        } finally {
            apiClient = null;
        }

    }


    public void setCbSupplierGSTN(ActionEvent actionEvent) {
        Object[] object = new Object[1];
        object[0] = cbSupplierGSTIN.getSelectionModel().getSelectedItem();
        if (object[0] != null) {
            for (CommonDTO commonDTO : supplierGSTINList) {
                if (object[0].equals(commonDTO)) {
                    supplierGSTIN_ID = commonDTO.getId();
                    gst_no = commonDTO.getText();
                }
            }
        }
    }

    public void onClickAdditionalCharges() {
        Stage stage = (Stage) purScrollPane.getScene().getWindow();
        AdditionalCharges.additionalCharges(stage, addChargesDTOList, total_taxable_amt, (input1, input2) -> {
            addChargesDTOList.clear();
            addChargesDTOList.addAll(input2);
            tfAddCharges.setText(input1[1]);
            PurchaseInvoiceCalculation.additionalChargesCalculation(input1[1], tvPurchaseInvoiceTable, callback);

        });
    }

    private void openAdjustBillWindow() {
//        Stage stage = (Stage) .getScene().getWindow()

        SingleInputDialogs.openAdjustBillPopUp(Communicator.stage, "Settlement of Bills", input -> {

            String debitNoteNo = (String) input[0];
            String debitNoteDate = (String) input[1];
            String id = (String) input[2];
            String totalAmt = (String) input[3];

        });
    }

    public void onClickSubmit(ActionEvent actionEvent) {
        invoiceValidations();

    }

    private void invoiceValidations() {
        //openAdjustBillWindow();
        Stage stage = (Stage) purScrollPane.getScene().getWindow();
        if (tfLedgerName.getText().isEmpty()) {
            AlertUtility.AlertDialogForErrorWithStage(stage, "Validation Error", "Enter mandatory Ledger Name Field ", output -> {
                if (output) {
                    tfLedgerName.requestFocus();
                }
            });
        } else if (tfInvoiceNo.getText().isEmpty()) {
            AlertUtility.AlertDialogForErrorWithStage(stage, "Validation Error", "Enter mandatory " + "Invoice No Field", output -> {
                if (output) {
                    tfInvoiceNo.requestFocus();
                }
            });
        } else if (tfInvoiceDate.getText().isEmpty()) {
            AlertUtility.AlertDialogForErrorWithStage(stage, "Validation Error", "Enter mandatory " + "Invoice Date Field", output -> {
                if (output) {
                    tfInvoiceDate.requestFocus();
                }
            });
        } else {
            String msg = "Do you want to create ";
            if (purchase_id > 0) msg = "Do you want to update ";
            if (purPrdRedId > 0) msg = "Do you want to update ";
            AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, msg + tfInvoiceNo.getText() + " Invoice Number?", input -> {
                if (input == 1) {
                    createPurchaseInvoice();
                }
            });

        }

    }

    private void validatePurchaseInvoiceNo() {

        try {
            logger.debug("Validating Pur Invoice Number");
            Map<String, String> map = new HashMap<>();
            map.put("supplier_id", ledger_id);
            map.put("bill_no", tfInvoiceNo.getText());
            System.out.println("Ledger Id:" + ledger_id);
            System.out.println("Bill No:" + tfInvoiceNo.getText());
            String formData = Globals.mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.VALIDATE_PUR_INV_NUMBER);
            String resBody = response.body();
            JsonObject obj = new Gson().fromJson(resBody, JsonObject.class);
            if (obj.get("responseStatus").getAsInt() == 409) {
                AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeWarning, obj.get("message").getAsString(), callback -> {
                    logger.debug("Validate Pur Invoice Success");
                });
                tfInvoiceNo.setText(tfInvoiceNo.getText());
                tfInvoiceNo.requestFocus();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in Purchase invoice validate():" + exceptionAsString);
        }

    }

    private void validatePurchaseInvoiceUpdate() {
        try {
            logger.debug("Validating Pur Invoice Number in Edit");
            Map<String, String> map = new HashMap<>();
            map.put("supplier_id", ledger_id);
            map.put("bill_no", tfInvoiceNo.getText());
            map.put("invoice_id", "" + purchase_id);
            String formData = Globals.mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.VALIDATE_PUR_INV_NUMBER_EDIT);
            String resBody = response.body();
            JsonObject obj = new Gson().fromJson(resBody, JsonObject.class);
            if (obj.get("responseStatus").getAsInt() == 409) {
                AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeWarning, obj.get("message").getAsString(), callback -> {
                    logger.debug("Validate Pur Invoice Success");
                });
                tfInvoiceNo.setText(tfInvoiceNo.getText());
                tfInvoiceNo.requestFocus();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in Purchase invoice validate():" + exceptionAsString);
        }

    }

    private void validateInvoiceDate() {
        try {
            logger.debug("Initiate the validateInvoiceDate() method");
            Map<String, String> map = new HashMap<>();
            String invoiceDate = Communicator.text_to_date.fromString(tfInvoiceDate.getText()).toString();
            map.put("invoiceDate", invoiceDate);
            String formData = Globals.mapToStringforFormData(map);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.VALIDATE_PUR_INV_DATE);
            String resBody = response.body();
            JsonObject obj = new Gson().fromJson(resBody, JsonObject.class);
            if (obj.get("responseStatus").getAsInt() == 404) {
                AlertUtility.AlertWarningTimeout(AlertUtility.alertTypeWarning, obj.get("message").getAsString(), callback -> {
                    logger.debug("Invoice Date : " + invoiceDate + " not in the Financial Year in Pur Invoice");
                    logger.debug("END of validateInvoiceDate() method in Pur Invoice");
                });
                tfInvoiceDate.requestFocus();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            logger.error("Exception in validateInvoiceDate:" + exceptionAsString);
        }

    }

    public void createPurchaseInvoice() {

        Object idValue = invoice_data_map.get("id");

        if (idValue != null) {
            purchase_invoice_map.put("id", idValue.toString());
            purchase_invoice_map.put("rowDelDetailsIds", deletedRows.toString());

        }
        else if(isRedirect==true){
            System.out.println("purchase_id0"+purPrdRedId);
             idValue = purPrdRedId;
             purchase_id=purPrdRedId;
            System.out.println("purchase_id0----------"+purchase_id);
            purchase_invoice_map.put("id", idValue.toString());
        }

        purchase_invoice_map.put("invoice_date", Communicator.text_to_date.fromString(tfInvoiceDate.getText()).toString());

        purchase_invoice_map.put("transaction_date", Communicator.text_to_date.fromString(tfTranxDate.getText()).toString());

        purchase_invoice_map.put("newReference", "false");

        purchase_invoice_map.put("invoice_no", tfInvoiceNo.getText());

        purchase_invoice_map.put("purchase_id", purchaseAcID);

        purchase_invoice_map.put("purchase_sr_no", tfPurchaseSerial.getText());

        purchase_invoice_map.put("supplier_code_id", ledger_id);

        purchase_invoice_map.put("isRoundOffCheck", String.valueOf(chbPurInvRoundOff.isSelected()));
        purchase_invoice_map.put("roundoff", lblPurInvRoundOff.getText());

        purchase_invoice_map.put("totalamt", totalamt);

        purchase_invoice_map.put("total_purchase_discount_amt", total_purchase_discount_amt);

        purchase_invoice_map.put("gstNo", gst_no);

        purchase_invoice_map.put("paymentMode", selectedPayMode);

        purchase_invoice_map.put("tcs_per", "0.00");

        purchase_invoice_map.put("tcs_amt", "0.00");

        purchase_invoice_map.put("tcs_mode", "");

        purchase_invoice_map.put("narration", tfNarration.getText());

        purchase_invoice_map.put("purchase_discount", purchase_discount.getText().isEmpty() || purchase_discount == null ? "0.0" : purchase_discount.getText());

        purchase_invoice_map.put("purchase_discount_amt", purchase_discount_amt.getText().isEmpty() || purchase_discount == null ? "0.0" : purchase_discount_amt.getText());

        purchase_invoice_map.put("additionalChargesTotal", "0.0");

        purchase_invoice_map.put("additionalCharges", addChargesDTOList.toString());


        List<PurchaseInvoiceTable> currentItems = new ArrayList<>(tvPurchaseInvoiceTable.getItems());

        if (!currentItems.isEmpty()) {
            PurchaseInvoiceTable lastItem = currentItems.get(currentItems.size() - 1);

            if (lastItem.getParticulars() != null && lastItem.getParticulars().isEmpty()) {
                currentItems.remove(currentItems.size() - 1);
            }
        }

        List<PurchaseInvoiceTable> list = new ArrayList<>(currentItems);


        //   List<PurchaseInvoiceTable> list = tvPurchaseInvoiceTable.getItems();

        for (PurchaseInvoiceTable purchaseInvoiceTable : list) {
            if (!purchaseInvoiceTable.getTotal_taxable_amt().equals("0.0")) {
                purchaseInvoiceTable.setTotal_amt(purchaseInvoiceTable.getTotal_taxable_amt());
            } else {
                purchaseInvoiceTable.setTotal_amt(purchaseInvoiceTable.getTaxable_amt());
            }
        }

        Double total_row_gross_amt = 0.0;

        for (PurchaseInvoiceTable purchaseInvoiceTable : list) {
            total_row_gross_amt = total_row_gross_amt + Double.parseDouble(purchaseInvoiceTable.getGross_amount1());
        }

        purchase_invoice_map.put("total_row_gross_amt", String.valueOf(total_row_gross_amt));

        purchase_invoice_map.put("row", list.toString());

        System.out.println(purchase_invoice_map);

        try {
            String formdata = mapToStringforFormData(purchase_invoice_map);

            Map<String, String> headers = new HashMap<>();
            headers.put("branch", "gvhm001");

            String response = null;
            APIClient apiClient = null;
            System.out.println("onsubmit purchase iud"+purchase_id);
            if (purchase_id > 0) {
                System.out.println("Hello purchase_id"+purchase_id);
//                response = APIClient.postMultipartRequest(purchase_invoice_map, null, "edit_purchase_invoices", headers);
                apiClient = new APIClient(EndPoints.EDIT_PUR_INVOICE_END, purchase_invoice_map, headers, null, RequestType.MULTI_PART);

            } else {
                apiClient = new APIClient(EndPoints.CREATE_PUR_INVOICE_END, purchase_invoice_map, headers, null, RequestType.MULTI_PART);
            }
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    String msg = "";
                    if (purchase_id > 0) {
                        PurchaseInvoiceListController.focusStatus="edit";
                        PurchaseInvoiceListController.selectedRowIndex= selectedRowIndex;
                        System.out.println(" selectedRowIndex in edit " + selectedRowIndex + " focusStatus " + "edit");
                        logger.info("Purchase Invoice update---> on Succeeded ");
                    } else {
                        PurchaseInvoiceListController.focusStatus="create";
                        logger.info("Purchase Invoice create---> on Succeeded ");
                        System.out.println(" selectedRowIndex in create " + selectedRowIndex + " focusStatus " + "create");
                    }
                    String response = workerStateEvent.getSource().getValue().toString();
                    JsonObject resDTO = new Gson().fromJson(response, JsonObject.class);
                    AlertUtility.CustomCallback callback = (number) -> {
                        if (number == 1) {
                            GlobalController.getInstance().addTabStatic(PURCHAE_INV_LIST_SLUG, false);
//                            GlobalController.getInstance().addTabStatic("product-list", false);
                        }
                    };
                    if (resDTO.get("responseStatus").getAsInt() == 200) {
                        msg = resDTO.get("message").getAsString();
                        Stage stage = (Stage) purScrollPane.getScene().getWindow();
                        AlertUtility.AlertSuccessTimeout("Success", msg, callback);

                    }

                }
            });
            apiClient.start();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void purc_disc_per(KeyEvent keyEvent) {
        CommonValidationsUtils.restrictToDecimalNumbers(purchase_discount);
        String discText = purchase_discount.getText();
        if (!TextUtils.isEmpty(discText)) {
            double totalTaxableAmt = 0.0;
            for (int i = 0; i < tvPurchaseInvoiceTable.getItems().size(); i++) {
                System.out.println("Gross amt in Prop--->" + tvPurchaseInvoiceTable.getItems().get(i).getGross_amount());
                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(i, tvPurchaseInvoiceTable, callback);//call row calculation function
                PurchaseInvoiceTable purchaseInvoiceTable = tvPurchaseInvoiceTable.getItems().get(i);
                totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());

            }
            double disc_per = Double.parseDouble(discText);
            System.out.println("totalTaxableAmount >> : " + totalTaxableAmt);
            Double amount = (totalTaxableAmt * disc_per) / 100;
            purchase_discount_amt.setText(String.valueOf(amount));
            PurchaseInvoiceCalculation.discountPropotionalCalculation(disc_per + "", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            if (!tfAddCharges.getText().isEmpty()) {
                System.out.println("Additioanal Charges Called >> ");
                PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            }
        } else {
            purchase_discount_amt.setText("");
            PurchaseInvoiceCalculation.discountPropotionalCalculation("0", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            if (!tfAddCharges.getText().isEmpty()) {
                System.out.println("Additioanal Charges Called >> ");
                PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            }
        }

        //? Calculate Tax for each Row
        PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);

    }

    public void purc_disc_amt(KeyEvent keyEvent) {
        CommonValidationsUtils.restrictToDecimalNumbers(purchase_discount_amt);
        String discAmtText = purchase_discount_amt.getText();
        if (!TextUtils.isEmpty(discAmtText)) {
            double totalTaxableAmt = 0.0;
            for (int i = 0; i < tvPurchaseInvoiceTable.getItems().size(); i++) {
                System.out.println("Gross amt in Prop--->" + tvPurchaseInvoiceTable.getItems().get(i).getGross_amount());
                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(i, tvPurchaseInvoiceTable, callback);//call row calculation function
                PurchaseInvoiceTable purchaseInvoiceTable = tvPurchaseInvoiceTable.getItems().get(i);
                totalTaxableAmt = totalTaxableAmt + Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());

            }
            double disc_amt = Double.parseDouble(discAmtText);
            double percentage = (disc_amt / totalTaxableAmt) * 100;
            purchase_discount.setText(String.format("%.2f", percentage));

            PurchaseInvoiceCalculation.discountPropotionalCalculation("0", disc_amt + "", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            if (!tfAddCharges.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            }
        } else {
            purchase_discount.setText("");
            PurchaseInvoiceCalculation.discountPropotionalCalculation("0", "0", tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            if (!tfAddCharges.getText().isEmpty()) {
                System.out.println("Additioanal CHarges Called >> ");
                PurchaseInvoiceCalculation.additionalChargesCalculation(tfAddCharges.getText(), tvPurchaseInvoiceTable, callback);
            }
        }

        //? Calculate Tax for each Row
        PurchaseInvoiceCalculation.calculateGst(tvPurchaseInvoiceTable, callback);
    }

    public void onClickCancel(ActionEvent actionEvent) {
        if(purchase_id>0){
            PurchaseInvoiceListController.focusStatus="edit";
            PurchaseInvoiceListController.selectedRowIndex= selectedRowIndex;
            System.out.println(" selectedRowIndex in edit Cancel " + selectedRowIndex + " focusStatus " + "edit");
            AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure want to Cancel and go to List ?", input -> {
                if (input == 1) {
                    if(batchIdToDelete!=null &&  !batchIdToDelete.equals("") ){
                        deleteProductBatch(batchIdToDelete);
                    }
                    GlobalController.getInstance().addTabStatic(PURCHAE_INV_LIST_SLUG, false);
                }
            });
        }
        else{
            AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure want to Cancel ?", input -> {
                if (input == 1) {
                    clearLedAndProdInfo();
                }
            });
        }


    }

    public void clearLedAndProdInfo() {
//        Debtor_id = "";
        tfLedgerName.setText("");
        cbSupplierGSTIN.getSelectionModel().clearSelection();
        tfNarration.setText("");
        tfInvoiceNo.setText("");
//        cbSupplierGSTIN.getItems().clear();
        //to Focus on Selected Product passed ProductId. on Clear pass blank
        SingleInputDialogs.productId = "";
        SingleInputDialogs.ledId = "";

        //? clear Ledger Info
        lblPurInvGstNo.setText("");
        lblPurInvArea.setText("");
        lblPurInvBank.setText("");
        lblPurInvContactPerson.setText("");
        lblPurInvFssai.setText("");
        lblPurInvLicenseNo.setText("");
        lblPurInvRoute.setText("");
        lblPurInvTransportName.setText("");
        lblPurInvCreditDays.setText("");

        //? clear Product Info
        lblPurInvProductBrand.setText("");
        lblPurInvProductGroup.setText("");
        lblPurInvProductSubGroup.setText("");
        lblPurInvProductCategory.setText("");
        lblPurInvProductHsn.setText("");
        lblPurInvProductTaxType.setText("");
        lblPurInvProductTaxPer.setText("");
        lblPurInvMarginPer.setText("");
        lblPurInvCost.setText("");
        lblPurInvProductShelfId.setText("");
        lblPurInvProductMaxStock.setText("");
        lblPurInvProductMinStock.setText("");

        //? clear Batch Info
        lblPurInvBatchNo.setText("");
        lblPurInvMfgDate.setText("");
        lblPurInvExpDate.setText("");
        lblPurInvPurRate.setText("");
        lblPurInvCost.setText("");
        lblPurInvCostWithTax.setText("");
        lblPurInvMrp.setText("");
        lblPurInvFsrmh.setText("");
        lblPurInvFssai.setText("");
        lbPurInvBatchCsrmh.setText("");
        lbPurInvBatchCsrai.setText("");
        lblPurInvFreeqty.setText("");
        lblPurInvMarginPer.setText("");

        //? clear all row calculation
        purchase_discount.setText("");

        purchase_discount_amt.setText("");

        lbGrossTotal.setText("0.00");

        lbDiscount.setText("0.00");

        lbTotal.setText("0.00");
        lbTax.setText("0.00");
        lbBillAmount.setText("0.00");
        lblPurInvRoundOff.setText("");
        tvPurchaseInvoiceTable.getItems().clear();// Add a new blank row if needed
        tvPurchaseInvoiceTable.getItems().add(new PurchaseInvoiceTable("", "0", "1", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        tfLedgerName.requestFocus();
        tvInvoiceProductHistory.getItems().clear();
        PurInvoiceCommunicator.resetFields();//new 2.0 to Clear the CMPTROW ComboBox
        tvGST_Table.getItems().clear();
        lbtotalFreeQty.setText("");
        lbtotalQty.setText("");
        //? Delete Created Batch

        if(batchIdToDelete!=null &&  !batchIdToDelete.equals("") ){
            deleteProductBatch(batchIdToDelete);
        }

    }

    public void deleteProductBatch(int id) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("id", String.valueOf(id));
            String formData = Globals.mapToStringforFormData(map);
            System.out.println("batch deletion Form DataInput "+formData );
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.DELETE_PRODUCT_BATCH);
            String responseBody = response.body();
//                    System.out.println(" responseBody of Batch delete " + responseBody);
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
            String message = jsonObject.get("message").getAsString();

            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                System.out.println("batch Successfully deleted");
            } else {
                System.out.println("batch deletion failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void tranxBatchDetailsFun(String bId) {
        APIClient apiClient = null;
        tranxPurInvTabPane.getSelectionModel().select(tabPurInvBatch);
        Map<String, String> map = new HashMap<>();
      //  map.put("batchNo", bNo);
        map.put("id", bId);
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("FormData: " + formData);
        logger.debug("Form Data for GET_BATCH_DETAILS_ENDPOINT in Purchase Invoice Controller:" + formData);
        logger.debug("Network call started GET_BATCH_DETAILS_ENDPOINT in Purchase Invoice Controller:");

        apiClient = new APIClient(EndPoints.GET_BATCH_DETAILS_ENDPOINT, formData, RequestType.FORM_DATA);

        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                if (jsonObject.get("responseStatus").getAsInt() == 200) {
                    JsonObject batchJsonObj = jsonObject.getAsJsonObject("response");
                    lblPurInvBatchNo.setText(batchJsonObj.get("batchNo").getAsString());
                    lblPurInvMfgDate.setText(DateConvertUtil.convertDispDateFormat(batchJsonObj.get("mfgDate").getAsString()));
                    lblPurInvExpDate.setText(DateConvertUtil.convertDispDateFormat(batchJsonObj.get("expDate").getAsString()));
               //     int barcode =(int) Double.parseDouble(batchJsonObj.get("barcode").getAsString());
                   // lblPurInvBarcode.setText(String.valueOf(barcode));
                    lblPurInvPurRate.setText(batchJsonObj.get("pur_rate").getAsString());
                    lblPurInvCost.setText(batchJsonObj.get("cost").getAsString());
                    lblPurInvCostWithTax.setText(batchJsonObj.get("cost_with_tax").getAsString());
                    lblPurInvMrp.setText(batchJsonObj.get("batchMrp").getAsString());
                    lblPurInvFsrmh.setText(batchJsonObj.get("fsrmh").getAsString());
                    lblPurInvFssai.setText(batchJsonObj.get("fsrai").getAsString());
                    lbPurInvBatchCsrmh.setText(batchJsonObj.get("csrmh").getAsString());
                    lbPurInvBatchCsrai.setText(batchJsonObj.get("csrai").getAsString());
                    lblPurInvFreeqty.setText(batchJsonObj.get("free_qty").getAsString());
                    lblPurInvMarginPer.setText(batchJsonObj.get("min_margin").getAsString());
                    logger.debug("Network call Success GET_BATCH_DETAILS_ENDPOINT:");

                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                logger.error("Network API cancelled in tranxBatchDetailsFun()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                logger.error("Network API failed in tranxBatchDetailsFun()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        logger.debug("tranxBatchDetailsFun() Ended");
    }

    public void handleCmbPaymentMode(ActionEvent actionEvent) {
        selectedPayMode = cmbPaymentMode.getValue();

    }
}

class TextFieldTableCellForPurInvoice extends TableCell<PurchaseInvoiceTable, String> {
    private final TextField textField;
    private final String columnName;

    private final TableCellCallback<Object[]> callback;
    private TableCellCallback<Object[]> productID_callback;

    TableCellCallback<Object[]> addPrdCalbak;

    private TextField button;


    public TextFieldTableCellForPurInvoice(String columnName, TableCellCallback<Object[]> callback) {
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

    public TextFieldTableCellForPurInvoice(String columnName, TableCellCallback<Object[]> callback, TableCellCallback<Object[]> productID_callback, TextField button) {
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


    public TextFieldTableCellForPurInvoice(String columnName, TableCellCallback<Object[]> callback, TableCellCallback<Object[]> productID_callback, TextField button,TableCellCallback<Object[]> addPrdCalbak) {
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
        if ("tcBatch".equals(columnName)) {
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

                    if (!textField.getText().isEmpty()) {
                        if(getIndex() == 0) {
                            if (getIndex() == getTableView().getItems().size() - 1) {
                                getTableView().getItems().add(new PurchaseInvoiceTable("", String.valueOf(getIndex() + 1), String.valueOf(getIndex() + 1), "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                            }
                            TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                            getTableView().edit(getIndex() + 1, colName);
                        }
                        else {
                                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(16);
                                getTableView().edit(getIndex(), colName);
                        }

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
        if ("tcParticulars".equals(columnName)) {
            textField.setEditable(false);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.SPACE) {
                    openProduct();
                }
                if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    if (getIndex() == 1) {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(7);
                        getTableView().edit(getIndex() - 1, colName);
                        event.consume();
                    } else {
                        TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(16);
                        getTableView().edit(getIndex() - 1, colName);
                        event.consume();
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

        if ("tcNetAmount".equals(columnName)) {
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

        SingleInputDialogs.openProductPopUpForPurchase(Communicator.stage, getTableView().getItems().get(getIndex()).getParticulars(),"Product", input -> {

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

            List<LevelAForPurInvoice> levelAForPurInvoiceList = ProductUnitsPacking.getAllProductUnitsPackingFlavour(productId);
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
        },item -> {
            if (item) {
                if (addPrdCalbak != null) {
                    int cur_index = getTableRow().getIndex();
                    Object[] object = new Object[3];
                    object[0] = true;
                    object[1] = cur_index;
                    object[2]="isProduct";
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
                product_details_to_batch_window.put("mrp",  unitRateList.getMrp() == 0.0 ? "": String.valueOf(unitRateList.getMrp()));
                product_details_to_batch_window.put("purchaserate", unitRateList.getPurRate() == 0.0 ? "": String.valueOf(unitRateList.getPurRate()));
            }
        }

        Long ledgerid = Long.parseLong(getTableView().getItems().get(0).getLedger_id());

        if (current_table_row.getIs_batch().equals("true") && ledgerid != 0) {
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

                    if (!batchWindowTableDTOSs.getExpiryDate().isEmpty() && batchWindowTableDTOSs.getExpiryDate()!=null ) {
                        System.out.println("hellllo I am Here " + batchWindowTableDTOSs.getExpiryDate());
                        getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_expiry(String.valueOf(Communicator.text_to_date.fromString(batchWindowTableDTOSs.getExpiryDate())));
                    } else {
                        getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setB_expiry("");

                    }
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_a(batchWindowTableDTOSs.getFsr_mh().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_b(batchWindowTableDTOSs.getFsr_ai().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_ai());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_c(batchWindowTableDTOSs.getCsr_mh().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_d(batchWindowTableDTOSs.getCsr_ai().isEmpty() ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_ai());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_a(batchWindowTableDTOSs.getFsr_mh().isEmpty() || batchWindowTableDTOSs.getFsr_mh().equals(null) ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_b(batchWindowTableDTOSs.getFsr_ai().isEmpty() || batchWindowTableDTOSs.getFsr_ai().equals(null) ? String.valueOf(0.0) : batchWindowTableDTOSs.getFsr_ai());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_c(batchWindowTableDTOSs.getCsr_mh().isEmpty() || batchWindowTableDTOSs.getCsr_mh().equals(null) ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_mh());
                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setRate_d(batchWindowTableDTOSs.getCsr_ai().isEmpty() || batchWindowTableDTOSs.getCsr_ai().equals(null) ? String.valueOf(0.0) : batchWindowTableDTOSs.getCsr_ai());

                    getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setMin_margin(batchWindowTableDTOSs.getMargin());
                    if (!batchWindowTableDTOSs.getManufacturingDate().isEmpty() && batchWindowTableDTOSs.getManufacturingDate()!=null) {
                        getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setManufacturing_date(String.valueOf(Communicator.text_to_date.fromString(batchWindowTableDTOSs.getManufacturingDate())));

                    }else{
                        getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).setManufacturing_date("");

                    }
                }
                String batchNo = getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).getBatch_or_serial();
                String bDetailsId = getTableView().getItems().get(Integer.parseInt(input.get(0).getCurrentIndex())).getB_details_id();

                PurchaseInvoiceCalculation.rowCalculationForPurcInvoice(Integer.parseInt(input.get(0).getCurrentIndex()), getTableView(), callback);

                PurchaseInvoiceCalculation.calculateGst(getTableView(), callback);

               // int index_e = Integer.parseInt(input.get(0).getCurrentIndex());

                if(getIndex() == getTableView().getItems().size()-1){
                    getTableView().getItems().addAll(new PurchaseInvoiceTable("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                    TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                    //System.out.println("Col Name:" + colName.getText() + "Index:" + getIndex());
                    getTableView().edit(getIndex() + 1, colName);
                }
                else {
               //     getTableView().getItems().addAll(new PurchaseInvoiceTable("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
                    TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                    System.out.println("Col Name:" + colName.getText() + "Index:" + getIndex());
                    getTableView().edit(getIndex() + 1, colName);
                }
                if (productID_callback != null) {
                    Object[] object = new Object[2];
                    object[0] = batchNo;
                    object[1] = bDetailsId;
                    productID_callback.call(object);
                }

            });
        }
    }

    public void textfieldStyle() {
        if (columnName.equals("tcParticulars")) {
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


        if (columnName.equals("tcParticulars")) {
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

        if (columnName.equals("tcNetAmount")) {
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
        if (item != null && columnName.equals("tcParticulars")) {
            ((PurchaseInvoiceTable) item).setParticulars(newValue.isEmpty() ? "" : newValue);
        } else if (columnName.equals("tcBatch")) {
            (getTableRow().getItem()).setBatch_or_serial(newValue.isEmpty() ? "" : newValue);
        }
    }
}

class ComboBoxTableCellForLevelA extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelA(String columnName) {

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

class ComboBoxTableCellForLevelB extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelBForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelB(String columnName) {
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

class ComboBoxTableCellForLevelC extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;
    ObservableList<LevelAForPurInvoice> comboList = null;

    public ComboBoxTableCellForLevelC(String columnName) {


        this.columnName = columnName;
        this.comboBox = new ComboBox<>();

        this.comboBox.setPromptText("Select");

//        comboBox.setPrefHeight(38);
//        comboBox.setMaxHeight(38);
//        comboBox.setMinHeight(38);
//
//        comboBox.setPrefWidth(139);
//        comboBox.setMaxWidth(139);
//        comboBox.setMinWidth(84);

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

class ComboBoxTableCellForUnit extends TableCell<PurchaseInvoiceTable, String> {

    String columnName;
    private final ComboBox<String> comboBox;

    public ComboBoxTableCellForUnit(String columnName) {


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

class ButtonTableCell extends TableCell<PurchaseInvoiceTable, String> {
    private final Button delete;

    public ButtonTableCell() {


        this.delete = createButtonWithImage();


        delete.setOnAction(actionEvent -> {
            PurchaseInvoiceTable table = getTableView().getItems().get(getIndex());
            getTableView().getItems().remove(table);
            getTableView().refresh();
        });

        delete.setOnKeyPressed(event -> {
            if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(7);
                getTableView().edit(getIndex(), colName);
            }

            if (event.getCode() == KeyCode.ENTER || !event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                getTableView().edit(getIndex() + 1, colName);
            }
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

        setGraphic(button);

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
        HBox vbox = new HBox(delete);
        vbox.setAlignment(Pos.CENTER);
        setGraphic(vbox);
        delete.requestFocus();

    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        HBox vbox = new HBox(delete);
        vbox.setAlignment(Pos.CENTER);
        setGraphic(vbox);
        delete.requestFocus();
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


class ButtonTableCellDelete extends TableCell<PurchaseInvoiceTable, String> {
    private final Button delete;
    TableCellCallback<Object[]> callback;

    TextField textField;

    public ButtonTableCellDelete(TableCellCallback<Object[]> DelCallback,TextField textField) {
        this.textField =textField;

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

        delete.setOnKeyPressed(event->{
            if (event.isShiftDown() && event.getCode() == KeyCode.TAB) {
                    TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(7);
                    getTableView().edit(getIndex(), colName);

            }
            if(!event.isShiftDown() && event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER){
                if(getIndex() == getTableView().getItems().size()-1) {
                    textField.requestFocus();
                }
                else {
                    TableColumn<PurchaseInvoiceTable, ?> colName = getTableView().getColumns().get(1);
                    getTableView().edit(getIndex() + 1, colName);
                }
            }
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
        HBox vbox = new HBox(delete);
        vbox.setAlignment(Pos.CENTER);
        setGraphic(vbox);
        delete.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        HBox vbox = new HBox(delete);
        vbox.setAlignment(Pos.CENTER);
        setGraphic(vbox);
    }


    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            if (getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                HBox vbox = new HBox(delete);
                vbox.setAlignment(Pos.CENTER);
                setGraphic(vbox);
            }
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

class ProductUnitsPacking {

    public static List<LevelAForPurInvoice> getAllProductUnitsPackingFlavour(String product_id) {


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

class PurchaseInvoiceCalculation {

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
        Map<Double, Double> igstTotals = new HashMap<>();//used to merge same sgst Percentage
        // Initialize totals to zero for each tax percentage
        for (Double taxPercentage : new Double[]{0.0, 3.0, 5.0, 12.0, 18.0, 28.0}) {
            cgstTotals.put(taxPercentage, 0.0);
            sgstTotals.put(taxPercentage, 0.0);
            igstTotals.put(taxPercentage, 0.0);
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
                if (!purchaseInvoiceTable.getFree().isEmpty() &&  purchaseInvoiceTable.getFree()!=null) {
                    freeQuantity = Double.parseDouble(purchaseInvoiceTable.getFree());

                } else {
                    freeQuantity = 0.0;
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
                igstTotals.put(taxPercentage, igstTotals.get(taxPercentage) + igst);

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
            double totalIGST = igstTotals.get(taxPer);

            if (totalCGST > 0) {
//                GstDTO gstDto :
                System.out.println("Tax Percentage: " + taxPer + "%");
                System.out.println("Total CGST: " + totalCGST);
                System.out.println("Total SGST: " + totalSGST);
                System.out.println();

                observableList.add(new GstDTO(String.format("%.2f", taxPer), String.format("%.2f", totalCGST), String.format("%.2f", totalSGST), String.format("%.2f", totalIGST)));

            }
        }


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
            object[8] = String.format("%.2f", totalFinalIgst);

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
            rowDisc = purchaseInvoiceTable.getFinal_dis_amt()!=null && !purchaseInvoiceTable.getFinal_dis_amt().isEmpty()? Double.parseDouble(purchaseInvoiceTable.getFinal_dis_amt()):0.0;
            System.out.println("netAmt--->" + purchaseInvoiceTable.getNet_amount());
            netAmt = purchaseInvoiceTable.getNet_amount()!=null && !purchaseInvoiceTable.getNet_amount().isEmpty()? Double.parseDouble(purchaseInvoiceTable.getNet_amount()):0.0;
            System.out.println("netAmt--->" + purchaseInvoiceTable.getTax());
            r_tax_per = purchaseInvoiceTable.getTax()!=null && !purchaseInvoiceTable.getTax().isEmpty()?Double.parseDouble(purchaseInvoiceTable.getTax()):0.0;
            rowTaxableAmt = Double.parseDouble(purchaseInvoiceTable.getTaxable_amt());
            r_tax_per = purchaseInvoiceTable.getTax()!=null && !purchaseInvoiceTable.getTax().isEmpty()?  Double.parseDouble(purchaseInvoiceTable.getTax()):0.0;
            rowTaxableAmt = purchaseInvoiceTable.getTaxable_amt()!=null && !purchaseInvoiceTable.getTaxable_amt().isEmpty()?  Double.parseDouble(purchaseInvoiceTable.getTaxable_amt()):0.0;
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
                r_tax_per = purchaseInvoiceTable.getTax()!=null && !purchaseInvoiceTable.getTax().isEmpty()? Double.parseDouble(purchaseInvoiceTable.getTax()):0.0;
                rowTaxableAmt = purchaseInvoiceTable.getTotal_taxable_amt()!=null && !purchaseInvoiceTable.getTotal_taxable_amt().isEmpty()? Double.parseDouble(purchaseInvoiceTable.getTotal_taxable_amt()):0.0;
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








