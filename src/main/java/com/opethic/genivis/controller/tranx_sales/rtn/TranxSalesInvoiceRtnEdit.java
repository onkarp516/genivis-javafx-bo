package com.opethic.genivis.controller.tranx_sales.rtn;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.controller.dialogs.AdditionalCharges;
import com.opethic.genivis.controller.dialogs.SingleInputDialogs;
import com.opethic.genivis.controller.tranx_sales.common.LineNumbersCellFactory;
import com.opethic.genivis.controller.tranx_sales.common.TranxCommonPopUps;
import com.opethic.genivis.dto.CommonDTO;
import com.opethic.genivis.dto.GstDetailsDTO;
import com.opethic.genivis.dto.SalesOrderSupplierDetailsDTO;
import com.opethic.genivis.dto.add_charges.CustomAddChargesDTO;
import com.opethic.genivis.dto.reqres.tranx.sales.invoice.TranxRowRes;
import com.opethic.genivis.dto.reqres.tranx.sales.invoice.TranxSalesInvoiceLastRecordRes;
import com.opethic.genivis.models.tranx.sales.*;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

import static com.opethic.genivis.utils.FxmFileConstants.*;

public class TranxSalesInvoiceRtnEdit implements Initializable {

    @FXML
    private Button btnTranxCancel;

    @FXML
    private Button btnTranxModify;

    @FXML
    private Button btnTranxSubmit;

    @FXML
    private ComboBox<?> cmbSaleInvCreateDeliveryType;

    @FXML
    private ComboBox<?> cmbSaleInvCreateSalesMan;

    @FXML
    private ComboBox<CommonDTO> cmbTranxSalesInvoiceCreateSalesAcc;

    @FXML
    private ComboBox<GstDetailsDTO> cmbTranxSalesInvoiceCreateSupplierGST;

    @FXML
    private DatePicker dpTranxSalesInvoiceCreateInvoiceDate;

    @FXML
    private Hyperlink hlSaleInvCreateChallan;

    @FXML
    private Hyperlink hlSaleInvCreateOrder;

    @FXML
    private Hyperlink hlSaleInvCreateQuotation;

    @FXML
    private TableView<?> tblGst;

    @FXML
    private TableView<TranxRow> tblTranxSalesInvoiceCreateCMPTRow;

    @FXML
    private TableColumn<TranxRow, Double> tcTranx1DisPer;

    @FXML
    private TableColumn<TranxRow, Double> tcTranx2DisPer;

    @FXML
    private TableColumn<TranxRow, Void> tcTranxAct;

    @FXML
    private TableColumn<TranxRow, String> tcTranxBatchNo;

    @FXML
    private TableColumn<?, ?> tcTranxCGST;

    @FXML
    private TableColumn<TranxRow, Double> tcTranxDisAmt;

    @FXML
    private TableColumn<TranxRow, Integer> tcTranxFreeQty;

    @FXML
    private TableColumn<?, ?> tcTranxGST;

    @FXML
    private TableColumn<TranxRow, String> tcTranxGrossAmt;

    @FXML
    private TableColumn<?, ?> tcTranxIGST;

    @FXML
    private TableColumn<TranxRow, String> tcTranxLevelA;

    @FXML
    private TableColumn<TranxRow, String> tcTranxLevelB;

    @FXML
    private TableColumn<TranxRow, String> tcTranxLevelC;

    @FXML
    private TableColumn<TranxRow, String> tcTranxNetAmt;

    @FXML
    private TableColumn<?, ?> tcTranxPackage;

    @FXML
    private TableColumn<?, ?> tcTranxParticulars;

    @FXML
    private TableColumn<TranxRow, Integer> tcTranxQty;

    @FXML
    private TableColumn<TranxRow, Double> tcTranxRate;

    @FXML
    private TableColumn<?, ?> tcTranxSGST;

    @FXML
    private TableColumn<?, ?> tcTranxSrNo;

    @FXML
    private TableColumn<TranxRow, String> tcTranxTax;

    @FXML
    private TableColumn<TranxRow, String> tcTranxUnit;

    @FXML
    private TextField tfTranxAdditionalTotal;

    @FXML
    private Label tfTranxBillAmtTotal;

    @FXML
    private TextField tfTranxCessTotal;

    @FXML
    private Label tfTranxDiscountTotal;

    @FXML
    private Label tfTranxFreeQty;

    @FXML
    private Label tfTranxGrossAmtTotal;

    @FXML
    private Label tfTranxRoundOff;

    @FXML
    private TextField tfTranxSaleInvCreateDisAmt;

    @FXML
    private TextField tfTranxSaleInvCreateDisPer;

    @FXML
    private TextField tfTranxSalesInvoiceCreateInvoiceNo;

    @FXML
    private TextField tfTranxSalesInvoiceCreateInvoiceSrNo;

    @FXML
    private TextField tfTranxSalesInvoiceCreateLedgerName, tfTranxSaleInvCreateNarration;

    @FXML
    private TextField tfTranxTCSAmt;

    @FXML
    private TextField tfTranxTCSPer;

    @FXML
    private Label tfTranxTaxTotal;

    @FXML
    private Label tfTranxTotal;

    @FXML
    private Label tfTranxTotalQty;

    @FXML
    private ScrollPane rootAnchorPane;

    @FXML
    private CheckBox chkRoundOff;

    private static final Logger loggerTranxSalesInvoiceEdit = LogManager.getLogger(TranxSalesInvoiceRtnEdit.class);
    private Stage stage = null;
    private String ledgerName = "";
    private String ledgerId = "";

    private List<TranxTaxCal> igstCal;
    private List<TranxTaxCal> cgstCal;
    private List<TranxTaxCal> sgstCal;

    private Integer editId = 0;

    private Integer salesDiscLedger = -1;
    private Integer selectedSalesInvoiceId;
    List<CustomAddChargesDTO> addChargesDTOList = new ArrayList<>();

    @FXML
    private TableView<SalesOrderSupplierDetailsDTO> tvInvoiceProductHistory;
    @FXML
    private TableColumn<?, ?> cmSupplierName;
    @FXML
    private TableColumn<?, ?> cmInvNo;
    @FXML
    private TableColumn<?, ?> cmInvDate;
    @FXML
    private TableColumn<?, ?> cmBatch;
    @FXML
    private TableColumn<?, ?> cmMRP;
    @FXML
    private TableColumn<?, ?> cmQty;
    @FXML
    private TableColumn<?, ?> cmRate;
    @FXML
    private TableColumn<?, ?> cmCost;
    @FXML
    private TableColumn<?, ?> cmDisPer;
    @FXML
    private TableColumn<?, ?> cmDisAmt;

    @FXML
    private TableView<TranxGst> tvGST_Table;
    @FXML
    private TableColumn<TranxGst, ?> tc_digst;
    @FXML
    private TableColumn<TranxGst, ?> tc_dgst;
    @FXML
    private TableColumn<TranxGst, ?> tc_igst;
    @FXML
    private TableColumn<TranxGst, ?> tc_cgst;
    @FXML
    private TableColumn<TranxGst, ?> tc_sgst;

    @FXML
    private TabPane tpTranxBottom;
    @FXML
    private Tab tbTranxLedgerBottom, tbTranxProductBottom, tbTranxBatchBottom;

    @FXML
    private Label lbTranxLedgerBottomGst, lbTranxLedgerBottomFSSAI, lbTranxLedgerBottomLicenseNo, lbTranxLedgerBottomBank, lbTranxLedgerBottomArea, lbTranxLedgerBottomRoute, lbTranxLedgerBottomContactPerson, lbTranxLedgerBottomTransport, lbTranxLedgerBottomCreditDays;

    @FXML
    private Label lbTranxProductBottomBrand, lbTranxProductBottomGroup, lbTranxProductBottomSubGroup, lbTranxProductBottomCategory, lbTranxProductBottomHSN, lbTranxProductBottomTaxType, lbTranxProductBottomTaxPer, lbTranxProductBottomMarginPer, lbTranxProductBottomCost, lbTranxProductBottomShelfId, lbTranxProductBottomMinStock, lbTranxProductBottomMaxStock;


    @FXML
    private Label lbTranxBatchBottomBNo, lbTranxBatchBottomMRP, lbTranxBatchBottomCessPer, lbTranxBatchBottomTotalAmt, lbTranxBatchBottomMFGDate, lbTranxBatchBottomPurRate, lbTranxBatchBottomCessAmt, lbTranxBatchBottomMinRateA, lbTranxBatchBottomAvailableSt, lbTranxBatchBottomDiscAmt, lbTranxBatchBottomMarginPer, lbTranxBatchBottomSalesRate;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //todo : autofocus on Search
        Platform.runLater(() -> dpTranxSalesInvoiceCreateInvoiceDate.requestFocus());
        //         Enter traversal
        rootAnchorPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
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
        initSalesInvoiceForm();
        initTableResponsive();
    }

    private void initSalesInvoiceForm() {
        ledgerId = "";
        ledgerName = "";
        tfTranxSalesInvoiceCreateLedgerName.clear();
        cmbTranxSalesInvoiceCreateSupplierGST.setValue(null);
        cmbTranxSalesInvoiceCreateSalesAcc.setValue(null);
        tfTranxSalesInvoiceCreateInvoiceNo.clear();
        tfTranxSalesInvoiceCreateInvoiceSrNo.clear();
        cmbSaleInvCreateSalesMan.setValue(null);
        cmbSaleInvCreateDeliveryType.setValue(null);
        tfTranxSaleInvCreateNarration.clear();
        tfTranxSaleInvCreateDisAmt.clear();
        tfTranxSaleInvCreateDisPer.clear();
        tfTranxTotalQty.setText("");
        tfTranxFreeQty.setText("");
        tfTranxGrossAmtTotal.setText("");
        tfTranxDiscountTotal.setText("");
        tfTranxTotal.setText("");
        tfTranxTaxTotal.setText("");
        tfTranxBillAmtTotal.setText("");
        tfTranxRoundOff.setText("");
        initSaleLastInvoiceNo();
        initSalesAcc();
        initLedgerSelection();
        initTableRow();
        initRoundOffCheckbox();
        initGstTable();
        initDiscLedger();
    }

    private void initDiscLedger() {
        tfTranxSaleInvCreateDisAmt.focusedProperty().addListener((obv, old, nw) -> {
            if (!nw) {
                TranxCal(false);
            }

        });
        tfTranxSaleInvCreateDisPer.focusedProperty().addListener((obv, old, nw) -> {
            if (!nw) TranxCal(true);
        });

        tfTranxAdditionalTotal.focusedProperty().addListener((obv, old, nw) -> {
            if (!nw) TranxCal(false);
        });

    }

    private void initRoundOffCheckbox() {
        chkRoundOff.setSelected(true);
        chkRoundOff.setOnAction((e) -> {
            TranxCal();
        });
    }


    private void initSaleLastInvoiceNo() {
        dpTranxSalesInvoiceCreateInvoiceDate.setValue(LocalDate.now());
        HttpResponse<String> response = APIClient.getRequest(EndPoints.TRANX_SALES_INVOICE_LAST_RECORD);
        TranxSalesInvoiceLastRecordRes tranxSalesInvoiceLastRecordRes = new Gson().fromJson(response.body(), TranxSalesInvoiceLastRecordRes.class);
        if (tranxSalesInvoiceLastRecordRes.getResponseStatus() == 200) {
            tfTranxSalesInvoiceCreateInvoiceNo.setText(tranxSalesInvoiceLastRecordRes.getSerialNo());
            tfTranxSalesInvoiceCreateInvoiceSrNo.setText("" + tranxSalesInvoiceLastRecordRes.getCount());
        }
    }

    private void initSalesAcc() {
        try {
            HttpResponse<String> response = APIClient.getRequest(EndPoints.SALESORDER_GET_SALES_ACCOUNTS);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            ObservableList<CommonDTO> observableList = FXCollections.observableArrayList();
            if (jsonObject.get("responseStatus").getAsInt() == 200) {
                JsonArray responseObject = jsonObject.getAsJsonArray("list");
                if (responseObject.size() > 0) {
                    for (JsonElement element : responseObject) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("id").getAsString();
                        String name = item.get("name").getAsString();
                        observableList.add(new CommonDTO(name, id));
                    }

                    cmbTranxSalesInvoiceCreateSalesAcc.setItems(observableList);
                    cmbTranxSalesInvoiceCreateSalesAcc.getSelectionModel().selectFirst();
                    cmbTranxSalesInvoiceCreateSalesAcc.setConverter(new StringConverter<CommonDTO>() {
                        @Override
                        public String toString(CommonDTO o) {
                            return o != null ? o.getText() : "";
                        }

                        @Override
                        public CommonDTO fromString(String s) {
                            return null;
                        }
                    });
                } else {
//                    System.out.println("responseObject is null");
                    loggerTranxSalesInvoiceEdit.info("initSalesAcc(): ResponseObject is null ");
                }
            } else {
                loggerTranxSalesInvoiceEdit.error("Exception on initSalesAcc(): ");
            }
        } catch (Exception e) {
            loggerTranxSalesInvoiceEdit.error("Exception on initSalesAcc(): " + Globals.getExceptionString(e));
        }
    }

    private void initLedgerSelection() {
        tfTranxSalesInvoiceCreateLedgerName.setOnMouseClicked(actionEvent -> {
            stage = (Stage) rootAnchorPane.getScene().getWindow();
            SingleInputDialogs.openSalesLedgerWithNamePopUp(stage, "Filter", ledgerName, input -> {
                loggerTranxSalesInvoiceEdit.debug("Callback to Get all the Details of the Selected ledger and Set it in the Fields", input);
                ledgerName = (String) input[0];
                ledgerId = (String) input[1];
                ObservableList<GstDetailsDTO> gstDetails = (ObservableList<GstDetailsDTO>) input[2];
                tfTranxSalesInvoiceCreateLedgerName.setText(ledgerName);
                if (gstDetails != null) {
                    setLedgerGSTDATA(gstDetails);
                }
                loggerTranxSalesInvoiceEdit.debug("Opened Ledger Bill Modal:On Clicking by selecting the Ledger Name ");
                tranxLedgerDetailsFun(ledgerId);
                TranxCal();
            });

        });
    }

    private void setLedgerGSTDATA(ObservableList<GstDetailsDTO> gstDetails) {
        if (gstDetails != null) {

            cmbTranxSalesInvoiceCreateSupplierGST.setItems(gstDetails);
            cmbTranxSalesInvoiceCreateSupplierGST.setConverter(new StringConverter<GstDetailsDTO>() {
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
            for (Object obj : cmbTranxSalesInvoiceCreateSupplierGST.getItems()) {
                GstDetailsDTO gstDetailsDTO = (GstDetailsDTO) obj;
                if (gstDetailsDTO.getId().equals(gstDetails.get(0).getId())) {
                    selectedGst = gstDetailsDTO;
                    break;
                }
            }
            if (selectedGst != null) {
                cmbTranxSalesInvoiceCreateSupplierGST.getSelectionModel().select(selectedGst);
            }
        }
    }

    private void initTableRow() {
        tcTranxSrNo.setCellFactory(new LineNumbersCellFactory());
        tcTranxParticulars.setCellValueFactory(new PropertyValueFactory<>("productName"));
//        tcTranxParticulars.setCellFactory(TextFieldTableCell.forTableColumn());
        tcTranxPackage.setCellValueFactory(new PropertyValueFactory<>("productPackage"));
        //! Level A
        tcTranxLevelA.setEditable(Globals.getUserControlsWithSlug("is_level_a"));
        tcTranxLevelA.setVisible(Globals.getUserControlsWithSlug("is_level_a"));
        tcTranxLevelA.setText(Globals.getUserControlsNameWithSlug("is_level_a") + "");
        tcTranxLevelA.setCellValueFactory(new PropertyValueFactory<>("dispLevelA"));
        tcTranxLevelA.setCellFactory(this::getLevelAOpts);
        tcTranxLevelA.setOnEditCommit((e) -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDispLevelA(e.getNewValue());
            tranxRow.getAllLevelAOpt().stream().filter(s -> s.getLabel().equalsIgnoreCase(e.getNewValue())).findAny().ifPresent(p -> {
                tranxRow.setSelectedLevelA(p);
                tranxRow.setDispLevelA(p.getLabel());
                tranxRow.setLevelAId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
            });
            moveNextColumn(e);
        });
        //! Level B
        tcTranxLevelB.setEditable(Globals.getUserControlsWithSlug("is_level_b"));
        tcTranxLevelB.setVisible(Globals.getUserControlsWithSlug("is_level_b"));
        tcTranxLevelB.setText(Globals.getUserControlsNameWithSlug("is_level_b") + "");
        tcTranxLevelB.setCellValueFactory(new PropertyValueFactory<>("dispLevelB"));
        tcTranxLevelB.setCellFactory(this::getLevelBOpts);
        tcTranxLevelB.setOnEditCommit((e) -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDispLevelB(e.getNewValue());
            tranxRow.getAllLevelBOpt().stream().filter(s -> s.getLabel().equalsIgnoreCase(e.getNewValue())).findAny().ifPresent(p -> {
                tranxRow.setSelectedLevelB(p);
                tranxRow.setDispLevelB(p.getLabel());
                tranxRow.setLevelBId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
            });
            moveNextColumn(e);
        });
        //! Level C
        tcTranxLevelC.setEditable(Globals.getUserControlsWithSlug("is_level_c"));
        tcTranxLevelC.setVisible(Globals.getUserControlsWithSlug("is_level_c"));
        tcTranxLevelC.setText(Globals.getUserControlsNameWithSlug("is_level_c") + "");
        tcTranxLevelC.setCellValueFactory(new PropertyValueFactory<>("dispLevelC"));
        tcTranxLevelC.setCellFactory(this::getLevelCOpts);
        tcTranxLevelC.setOnEditCommit((e) -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDispLevelC(e.getNewValue());
            tranxRow.getAllLevelCOpt().stream().filter(s -> s.getLabel().equalsIgnoreCase(e.getNewValue())).findAny().ifPresent(p -> {
                tranxRow.setSelectedLevelC(p);
                tranxRow.setDispLevelC(p.getLabel());
                tranxRow.setLevelCId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
            });
            moveNextColumn(e);
        });
        //! Unit
        tcTranxUnit.setCellValueFactory(new PropertyValueFactory<>("dispUnit"));
        tcTranxUnit.setCellFactory(this::getCellUnits);
        tcTranxUnit.setOnEditCommit((e) -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDispUnit(e.getNewValue());
            tranxRow.getAllUnitOpt().stream().filter(s -> s.getLabel().equalsIgnoreCase(e.getNewValue())).findAny().ifPresent(p -> {
                tranxRow.setSelectedUnit(p);
                tranxRow.setUnitConv(p.getUnitConversion());
                tranxRow.setUnitId(p.getUnitId());
                tranxRow.setDispUnit(p.getUnitName());
            });
            moveNextColumn(e);
        });
        tcTranxBatchNo.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getbNo()));
        tcTranxTax.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getIgst()));
        tcTranxGrossAmt.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getGrossAmt()));
        tcTranxNetAmt.setCellValueFactory(cellData -> new SimpleStringProperty("" + cellData.getValue().getTotalAmt()));
        tcTranxQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        tcTranxQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        tcTranxQty.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setQty(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranxFreeQty.setEditable(Globals.getUserControlsWithSlug("is_free_qty"));
        tcTranxFreeQty.setVisible(Globals.getUserControlsWithSlug("is_free_qty"));
        tcTranxFreeQty.setCellValueFactory(new PropertyValueFactory<>("freeqty"));
        tcTranxFreeQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        tcTranxFreeQty.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setFreeqty(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranxRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        tcTranxRate.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        tcTranxRate.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setRate(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranx1DisPer.setCellValueFactory(new PropertyValueFactory<>("disPer"));
        tcTranx1DisPer.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        tcTranx1DisPer.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDisPer(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranx2DisPer.setEditable(Globals.getUserControlsWithSlug("is_multi_discount"));
        tcTranx2DisPer.setVisible(Globals.getUserControlsWithSlug("is_multi_discount"));
        tcTranx2DisPer.setCellValueFactory(new PropertyValueFactory<>("disPer2"));
        tcTranx2DisPer.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        tcTranx2DisPer.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDisPer2(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranxDisAmt.setCellValueFactory(new PropertyValueFactory<>("disAmt"));
        tcTranxDisAmt.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        tcTranxDisAmt.setOnEditCommit(e -> {
            TranxRow tranxRow = e.getRowValue();
            tranxRow.setDisAmt(e.getNewValue());
            TranxCal();
            moveNextColumn(e);
        });
        tcTranxAct.setCellFactory((param) -> {
            final TableCell<TranxRow, Void> cell = new TableCell<>() {
                private ImageView delImg = Globals.getDelImage();
                private ImageView edtImg = Globals.getPlusImage();

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);
                }

                private final Button actionButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);

                {
                    actionButton.setPrefWidth(20.0);
                    actionButton.setPrefHeight(20.0);
                    actionButton.setOnAction(event -> {
                        // Handle button action here
                        TranxRow removeRow = tblTranxSalesInvoiceCreateCMPTRow.getItems().get(getIndex());
                        tblTranxSalesInvoiceCreateCMPTRow.getItems().remove(removeRow);
                    });
                    edtButton.setPrefWidth(10.0);
                    edtButton.setPrefHeight(10.0);
                    edtButton.setOnAction(event -> {
                        // Handle button action here
                        tblTranxSalesInvoiceCreateCMPTRow.getItems().add(GlobalTranx.newEmptyTranxRow());
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(actionButton);
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
        });
        tblTranxSalesInvoiceCreateCMPTRow.setEditable(true);
        tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().cellSelectionEnabledProperty().set(true);
        tblTranxSalesInvoiceCreateCMPTRow.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
//                if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
//                    System.out.println("SHIFT+TAB");
//                    TablePosition currentPosition = tblTranxSalesInvoiceCreateCMPTRow.getFocusModel().getFocusedCell();
//                    TableColumn<TranxRow, ?> previousColumn = currentPosition.getTableColumn();
//                    int previousIndex = previousColumn.getTableView().getColumns().indexOf(previousColumn) - 1;
//                    if (previousIndex >= 0) {
//                        TableColumn<TranxRow, ?> newColumn = tblTranxSalesInvoiceCreateCMPTRow.getColumns().get(previousIndex);
//                        tblTranxSalesInvoiceCreateCMPTRow.getFocusModel().focus(tblTranxSalesInvoiceCreateCMPTRow.getItems().size() - 1, newColumn);
//                    }
//                    event.consume();
//                }
                if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
//                    System.out.println("SHIFT+TAB");
                    TableView.TableViewFocusModel<TranxRow> focusModel = tblTranxSalesInvoiceCreateCMPTRow.getFocusModel();
                    TablePosition<TranxRow, ?> focusedCell = focusModel.getFocusedCell();
//                    System.out.println("focusedCell" + focusedCell);
                    if (focusedCell != null) {
                        int columnIndex = focusedCell.getColumn();
//                        System.out.println("columnIndex => " + columnIndex);
                        if (columnIndex > 0) {
                            TableColumn<TranxRow, ?> previousColumn = tblTranxSalesInvoiceCreateCMPTRow.getVisibleLeafColumns().get(columnIndex - 1);
                            tblTranxSalesInvoiceCreateCMPTRow.getFocusModel().focus(tblTranxSalesInvoiceCreateCMPTRow.getItems().size() - 1, previousColumn);
                        }
                    }
                    event.consume();
                }
            }
        });
        tblTranxSalesInvoiceCreateCMPTRow.getFocusModel().focusedCellProperty().addListener((obs, ov, nv) -> {
            int row = nv.getRow();
            int column = nv.getColumn();
            List<TableColumn<TranxRow, ?>> visibleColumns = tblTranxSalesInvoiceCreateCMPTRow.getVisibleLeafColumns();
            try {
                TableColumn<TranxRow, ?> vcolumn = visibleColumns.get(column);
//            System.out.println("vcolumn =->"+vcolumn.getId());
//            System.out.println("vcolumn =->"+vcolumn.getText());
                Stage stage = (Stage) rootAnchorPane.getScene().getWindow();
                TranxRow selectedRow = tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().getSelectedItem();
                switch (vcolumn.getId()) {
                    case "tcTranxParticulars":
                        TranxCommonPopUps.openProductPopUp(stage, selectedRow.getProductId(), "Product", input -> {
                            Integer selectedIndex = tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().getSelectedIndex();
//                            System.out.println("product selection set data -> " + input);
                            if (input != null) {
                                selectedRow.setProductName(input.getProductName());
                                selectedRow.setProductId(input.getProductId());
                                selectedRow.setProductPackage(input.getPackageName());
                                selectedRow.setDispUnit(input.getUnitName());
                                selectedRow.setRate(input.getSalesRate());
                                selectedRow.setIgst(input.getIgst());
                                selectedRow.setCgst(input.getCgst());
                                selectedRow.setSgst(input.getSgst());
                                selectedRow.setBatch(input.getBatch());
                                selectedRow.setSerial(input.getSerial());
                                selectedRow.setSelectedProduct(input);
                                UnitLevelLst unitLevelLst = GlobalTranx.getUnitsFromProductId(selectedRow.getProductId());
                                selectedRow.setUnitLevelLst(unitLevelLst);
                                LevelAOpts levelAOpts = unitLevelLst.getLevelAOpts().get(0);
                                selectedRow.setSelectedLevelA(levelAOpts);
                                if (levelAOpts.getValue().equalsIgnoreCase("")) {
                                    selectedRow.setLevelAId(0);
                                    selectedRow.setDispLevelA("");
                                } else {
                                    selectedRow.setLevelAId(Integer.valueOf(levelAOpts.getValue()));
                                    selectedRow.setDispLevelA(levelAOpts.getLabel());
                                }
                                List<LevelBOpts> lstLevelBOpts = levelAOpts.getLevelBOpts();

                                LevelBOpts levelBOpts = lstLevelBOpts.get(0);
                                selectedRow.setSelectedLevelB(levelBOpts);
                                if (levelBOpts.getValue().equalsIgnoreCase("")) {
                                    selectedRow.setLevelBId(0);
                                    selectedRow.setDispLevelB("");
                                } else {
                                    selectedRow.setLevelBId(Integer.valueOf(levelBOpts.getValue()));
                                    selectedRow.setDispLevelB(levelBOpts.getLabel());
                                }
                                List<LevelCOpts> lstLevelCOpts = levelBOpts.getLevelCOpts();
                                LevelCOpts levelCOpts = lstLevelCOpts.get(0);
                                selectedRow.setSelectedLevelC(levelCOpts);
                                if (levelCOpts.getValue().equalsIgnoreCase("")) {
                                    selectedRow.setLevelCId(0);
                                    selectedRow.setDispLevelC("");
                                } else {
                                    selectedRow.setLevelCId(Integer.valueOf(levelCOpts.getValue()));
                                    selectedRow.setDispLevelC(levelCOpts.getLabel());
                                }
                                List<UnitOpts> lstUnitOpts = levelCOpts.getUnitOpts();
                                UnitOpts unitOpts = lstUnitOpts.get(0);
                                selectedRow.setSelectedUnit(unitOpts);
                                selectedRow.setUnitId(unitOpts.getUnitId());
                                selectedRow.setUnitConv(unitOpts.getUnitConversion());
                            }

                            tblTranxSalesInvoiceCreateCMPTRow.getItems().set(selectedIndex, selectedRow);
                            getSupplierListbyProductId(String.valueOf(selectedRow.getProductId()));
                            tranxProductDetailsFun(String.valueOf(selectedRow.getProductId()));
                        });
                        break;
                    case "tcTranxBatchNo":
                        if (selectedRow.isBatch() == true) {
                            TranxCommonPopUps.openBatchPopUp(stage, selectedRow, dpTranxSalesInvoiceCreateInvoiceDate.getValue().toString(), "Batch", input -> {
                                Integer selectedIndex = tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().getSelectedIndex();
                                if (input != null) {
                                    selectedRow.setSelectedBatch(input);
                                    selectedRow.setbDetailId(input.getId());
                                    selectedRow.setbNo(input.getBatchNo());
//                                    selectedRow.setRate(input.minRateA);
                                }
                                tblTranxSalesInvoiceCreateCMPTRow.getItems().set(selectedIndex, selectedRow);
                                getBatchDetailsByBatchNoAndId(input.getBatchNo(), String.valueOf(input.getId()));

                            });
                        } else {
                            selectedRow.setbNo("#");
                        }
                        break;
                    default:
                        Platform.runLater(() -> {
                            tblTranxSalesInvoiceCreateCMPTRow.edit(row, vcolumn);
                        });
                        break;

                }

            } catch (Exception e) {
                loggerTranxSalesInvoiceEdit.error("Exception in getFocusModel(): " + Globals.getExceptionString(e));
            }
        });
        tblTranxSalesInvoiceCreateCMPTRow.getItems().clear();
        tblTranxSalesInvoiceCreateCMPTRow.getItems().add(GlobalTranx.newEmptyTranxRow());

    }

    public void initTableResponsive() {
        tcTranxSrNo.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxPackage.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxUnit.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxQty.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranx1DisPer.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxDisAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxTax.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        tcTranxAct.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        if (tcTranxFreeQty.isVisible()) {
            tcTranxFreeQty.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxRate.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        } else {
            tcTranxRate.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.1));
        }

        if (tcTranxLevelA.isVisible() == true && tcTranxLevelB.isVisible() == false && tcTranxLevelC.isVisible() == false) {
//            System.out.println("inside first condn");
            tcTranxParticulars.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.3));
            tcTranxGrossAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.1));
            tcTranxNetAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxLevelA.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        } else if (tcTranxLevelA.isVisible() == true && tcTranxLevelB.isVisible() == true && tcTranxLevelC.isVisible() == false) {
//            System.out.println("inside second condn");
            tcTranxLevelA.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxParticulars.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.28));
            tcTranxGrossAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.1));
            tcTranxNetAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxLevelB.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        } else if (tcTranxLevelA.isVisible() == true && tcTranxLevelB.isVisible() == true && tcTranxLevelC.isVisible() == true) {
//            System.out.println("inside thrd condn");
            tcTranxParticulars.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.23));
            tcTranxGrossAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.1));
            tcTranxNetAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxLevelA.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxLevelB.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
            tcTranxLevelC.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.05));
        } else {
//            System.out.println("inside fourth condn");
            tcTranxParticulars.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.35));
            tcTranxGrossAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.1));
            tcTranxNetAmt.prefWidthProperty().bind(tblTranxSalesInvoiceCreateCMPTRow.widthProperty().multiply(0.08));
        }

    }

    private void moveNextColumn(TableColumn.CellEditEvent<TranxRow, ?> e) {
        TranxRow tranxRow = e.getRowValue();
        int row = tblTranxSalesInvoiceCreateCMPTRow.getItems().indexOf(tranxRow);
        TableColumn<TranxRow, ?> tbColumnFocus = null;
        TablePosition<TranxRow, ?> position = null;
        int cnt = 1;
        if (!Globals.getUserControlsWithSlug("is_level_a")) ++cnt;
        if (!Globals.getUserControlsWithSlug("is_level_b")) ++cnt;
        if (!Globals.getUserControlsWithSlug("is_level_c")) ++cnt;
        if (!Globals.getUserControlsWithSlug("is_free_qty")) ++cnt;
        position = e.getTablePosition();
        int column = position.getColumn();
        tbColumnFocus = tblTranxSalesInvoiceCreateCMPTRow.getColumns().get(column + cnt);
        if (tbColumnFocus.getText().equalsIgnoreCase("Disc Amt") && !Globals.getUserControlsWithSlug("is_multi_discount"))
            ++cnt;

        Boolean isAct = false;
        while (true) {
            position = e.getTablePosition();
            column = position.getColumn();
//            System.out.println("column" + column);
//            System.out.println("cnt" + cnt);
            int cal = column + cnt;
//            System.out.println("cal ->" + cal);
            tbColumnFocus = tblTranxSalesInvoiceCreateCMPTRow.getColumns().get(cal);
//            System.out.println("clm  name ->" + tbColumnFocus.getText());
//            System.out.println("clmn id ->" + tbColumnFocus.getId());
            cnt++;
            if (tbColumnFocus.getText().equalsIgnoreCase("ACT")) {
                isAct = true;
                break;
            }
            if (tbColumnFocus.isEditable() == true) break;
        }
        if (isAct == false) {
            // Select the target cell
            tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().clearAndSelect(row, tbColumnFocus);
            // Request focus on the selected cell
            tblTranxSalesInvoiceCreateCMPTRow.requestFocus();
            TableColumn<TranxRow, ?> finalTbColumnFocus = tbColumnFocus;
            Platform.runLater(() -> {
//                System.out.println("final text ->" + finalTbColumnFocus.getText());
//                System.out.println("final clu" + finalTbColumnFocus.getId());
                tblTranxSalesInvoiceCreateCMPTRow.edit(row, finalTbColumnFocus);
            });
        } else {
            // Check if there is a next row present in the table view
            if ((tblTranxSalesInvoiceCreateCMPTRow.getItems().size()) > (row + 1)) {
//                System.out.println("There is a next row present in the table view.");
            } else {
                System.out.println("There is no next row present in the table view.");
                tblTranxSalesInvoiceCreateCMPTRow.getItems().add(GlobalTranx.newEmptyTranxRow());
            }
//            position = e.getTablePosition();
//            column = position.getColumn();
//            System.out.println("row -> " + row + "column -> " + column);
            tbColumnFocus = tblTranxSalesInvoiceCreateCMPTRow.getColumns().get(1);
//            System.out.println("tbColumnFocus -> " + tbColumnFocus.getText());
            // Select the target cell
            tblTranxSalesInvoiceCreateCMPTRow.getSelectionModel().clearAndSelect(row + 1, tbColumnFocus);
        }

    }

    private TableCell<TranxRow, String> getCellUnits(TableColumn<TranxRow, String> tableColumn) {
        return new ComboBoxTableCell<>() {
            @Override
            public void startEdit() {
                getItems().setAll(getTableRow().getItem().getAllUnits());
                super.startEdit();
            }
        };
    }

    private TableCell<TranxRow, String> getLevelCOpts(TableColumn<TranxRow, String> tableColumn) {
        return new ComboBoxTableCell<>() {
            @Override
            public void startEdit() {
                getItems().setAll(getTableRow().getItem().getAllLevelC());
                super.startEdit();
            }
        };
    }

    private TableCell<TranxRow, String> getLevelBOpts(TableColumn<TranxRow, String> tableColumn) {
        return new ComboBoxTableCell<>() {
            @Override
            public void startEdit() {
                getItems().setAll(getTableRow().getItem().getAllLevelB());
                super.startEdit();
            }
        };
    }

    private TableCell<TranxRow, String> getLevelAOpts(TableColumn<TranxRow, String> tableColumn) {
        return new ComboBoxTableCell<>() {
            @Override
            public void startEdit() {
                getItems().setAll(getTableRow().getItem().getAllLevelA());
                super.startEdit();
            }
        };
    }

    public void TranxCal() {
        List<TranxRow> rows = tblTranxSalesInvoiceCreateCMPTRow.getItems();
        Double ledgerDiscPer = !tfTranxSaleInvCreateDisPer.getText().isEmpty() ? Double.parseDouble(tfTranxSaleInvCreateDisPer.getText()) : 0.0;
        Double ledgerDiscAmt = !tfTranxSaleInvCreateDisAmt.getText().isEmpty() ? Double.parseDouble(tfTranxSaleInvCreateDisAmt.getText()) : 0.0;
        Double additionalChargesTotal = !tfTranxAdditionalTotal.getText().isEmpty() ? Double.parseDouble(tfTranxAdditionalTotal.getText()) : 0.0;
        TranxRtnCal tranxRtnCal = GlobalTranx.TranxCalculation(rows, false, ledgerDiscPer, ledgerDiscAmt, additionalChargesTotal);
        ObservableList<TranxRow> actRow = FXCollections.observableArrayList(tranxRtnCal.getRow());
        for (int i = 0; i < actRow.size(); i++) {
            TranxRow tranxRow = actRow.get(i);
            tblTranxSalesInvoiceCreateCMPTRow.getItems().set(i, tranxRow);
        }
        tfTranxGrossAmtTotal.setText(tranxRtnCal.getTotalBaseAmt() + "");
        tfTranxFreeQty.setText(tranxRtnCal.getTotalFreeQty() + "");
        tfTranxTotalQty.setText(tranxRtnCal.getTotalQty() + "");
        tfTranxDiscountTotal.setText(tranxRtnCal.getTotalDisAmt() + "");
        tfTranxTotal.setText(tranxRtnCal.getTotalAmt() + "");
        tfTranxTaxTotal.setText(tranxRtnCal.getTaxTotalAmt() + "");
        igstCal = tranxRtnCal.getTotalIgstCal();
        cgstCal = tranxRtnCal.getTotalCgstCal();
        sgstCal = tranxRtnCal.getTotalSgstCal();
        Double roundOffAmt = 0.0;
        Double rOffAmt = 0.0;
        Double finalAmt = 0.0;
        if (chkRoundOff.isSelected() == false) {
            roundOffAmt = GlobalTranx.TranxRoundDigit(tranxRtnCal.getBillAmt(), GlobalTranx.configDecimalPlace);
            rOffAmt = 0.0;
            finalAmt = tranxRtnCal.getBillAmt();
        } else {
            roundOffAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(Math.round(tranxRtnCal.getBillAmt())), GlobalTranx.configDecimalPlace);
            rOffAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(roundOffAmt - tranxRtnCal.getBillAmt()), GlobalTranx.configDecimalPlace);
            ;
            finalAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(Math.round(tranxRtnCal.getBillAmt())), GlobalTranx.configDecimalPlace);
        }

        tfTranxRoundOff.setText(rOffAmt + "");
        tfTranxBillAmtTotal.setText(finalAmt + "");
//        if (
//                (elementFrom.toLowerCase() == "roundoff" && isCal == true) ||
//                        isRoundOffCheck == false
//        ) {
//            roundoffamt = roundDigit(total_final_amt, configDecimalPlaces);
//            roffamt = 0;
//            bill_amount = roundDigit(bill_amount, configDecimalPlaces);
//        } else {
//            roundoffamt = Math.round(total_final_amt);
//            bill_amount = Math.round(parseFloat(bill_amount));
//            roffamt = roundDigit(roundoffamt - total_final_amt, configDecimalPlaces);
//        }
    }

    public void TranxCal(Boolean disPer) {
        List<TranxRow> rows = tblTranxSalesInvoiceCreateCMPTRow.getItems();
        Double ledgerDiscPer = !tfTranxSaleInvCreateDisPer.getText().isEmpty() ? Double.parseDouble(tfTranxSaleInvCreateDisPer.getText()) : 0.0;
        Double ledgerDiscAmt = !tfTranxSaleInvCreateDisAmt.getText().isEmpty() ? Double.parseDouble(tfTranxSaleInvCreateDisAmt.getText()) : 0.0;
        Double additionalChargesTotal = !tfTranxAdditionalTotal.getText().isEmpty() ? Double.parseDouble(tfTranxAdditionalTotal.getText()) : 0.0;
        TranxRtnCal tranxRtnCal = GlobalTranx.TranxCalculation(rows, disPer, ledgerDiscPer, ledgerDiscAmt, additionalChargesTotal);
        ObservableList<TranxRow> actRow = FXCollections.observableArrayList(tranxRtnCal.getRow());
        for (int i = 0; i < actRow.size(); i++) {
            TranxRow tranxRow = actRow.get(i);
            tblTranxSalesInvoiceCreateCMPTRow.getItems().set(i, tranxRow);
        }
        tfTranxGrossAmtTotal.setText(tranxRtnCal.getTotalBaseAmt() + "");
        tfTranxFreeQty.setText(tranxRtnCal.getTotalFreeQty() + "");
        tfTranxTotalQty.setText(tranxRtnCal.getTotalQty() + "");
        tfTranxDiscountTotal.setText(tranxRtnCal.getTotalDisAmt() + "");
        tfTranxTotal.setText(tranxRtnCal.getTotalAmt() + "");
        tfTranxTaxTotal.setText(tranxRtnCal.getTaxTotalAmt() + "");
        tfTranxSaleInvCreateDisPer.setText(tranxRtnCal.getActDiscPer() + "");
        tfTranxSaleInvCreateDisAmt.setText(tranxRtnCal.getActDiscAmt() + "");
        igstCal = tranxRtnCal.getTotalIgstCal();
        cgstCal = tranxRtnCal.getTotalCgstCal();
        sgstCal = tranxRtnCal.getTotalSgstCal();
        Double roundOffAmt = 0.0;
        Double rOffAmt = 0.0;
        Double finalAmt = 0.0;
        if (chkRoundOff.isSelected() == false) {
            roundOffAmt = GlobalTranx.TranxRoundDigit(tranxRtnCal.getBillAmt(), GlobalTranx.configDecimalPlace);
            rOffAmt = 0.0;
            finalAmt = tranxRtnCal.getBillAmt();
        } else {
            roundOffAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(Math.round(tranxRtnCal.getBillAmt())), GlobalTranx.configDecimalPlace);
            rOffAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(roundOffAmt - tranxRtnCal.getBillAmt()), GlobalTranx.configDecimalPlace);
            ;
            finalAmt = GlobalTranx.TranxRoundDigit(Double.valueOf(Math.round(tranxRtnCal.getBillAmt())), GlobalTranx.configDecimalPlace);
        }

        tfTranxRoundOff.setText(rOffAmt + "");
        tfTranxBillAmtTotal.setText(finalAmt + "");
//        if (
//                (elementFrom.toLowerCase() == "roundoff" && isCal == true) ||
//                        isRoundOffCheck == false
//        ) {
//            roundoffamt = roundDigit(total_final_amt, configDecimalPlaces);
//            roffamt = 0;
//            bill_amount = roundDigit(bill_amount, configDecimalPlaces);
//        } else {
//            roundoffamt = Math.round(total_final_amt);
//            bill_amount = Math.round(parseFloat(bill_amount));
//            roffamt = roundDigit(roundoffamt - total_final_amt, configDecimalPlaces);
//        }
        TranxCalGst();
    }

    public void TranxSubmit(ActionEvent actionEvent) {
        TranxCal();
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Do you want to Submit?", input -> {
            if (input == 1) callCreateSalesInvoiceAPI();
        });
    }

    private void callCreateSalesInvoiceAPI() {
        JSONArray rowArr = new JSONArray();
        for (int i = 0; i < tblTranxSalesInvoiceCreateCMPTRow.getItems().size(); i++) {
            TranxRow tRow = tblTranxSalesInvoiceCreateCMPTRow.getItems().get(i);
            if (!tRow.getProductName().isEmpty()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("details_id", tRow.getDetailId());
                jsonObject.put("productId", tRow.getProductId());
                jsonObject.put("levelaId", tRow.getLevelAId() > 0 ? String.valueOf(tRow.getLevelAId()) : "");
                jsonObject.put("levelbId", tRow.getLevelBId() > 0 ? String.valueOf(tRow.getLevelBId()) : "");
                jsonObject.put("levelcId", tRow.getLevelCId() > 0 ? String.valueOf(tRow.getLevelCId()) : "");
                jsonObject.put("unitId", tRow.getUnitId());
                jsonObject.put("qty", tRow.getQty());
                jsonObject.put("free_qty", tRow.getFreeqty());
                jsonObject.put("unit_conv", tRow.getUnitConv());
                jsonObject.put("rate", tRow.getRate());
                jsonObject.put("dis_amt", tRow.getDisAmt());
                jsonObject.put("dis_per", tRow.getDisPer());
                jsonObject.put("dis_per2", tRow.getDisPer2());
                jsonObject.put("row_dis_amt", tRow.getRowDisAmt());
                jsonObject.put("gross_amt", tRow.getGrossAmt());
                jsonObject.put("add_chg_amt", tRow.getAddChargesAmt());
                jsonObject.put("gross_amt1", tRow.getGrossAmt1());
                jsonObject.put("invoice_dis_amt", tRow.getInvoiceDisAmt());
                jsonObject.put("dis_per_cal", tRow.getDisPerCal());
                jsonObject.put("dis_amt_cal", tRow.getDisAmtCal());
                jsonObject.put("total_amt", tRow.getTotalAmt());
                jsonObject.put("igst", tRow.getIgst());
                jsonObject.put("cgst", tRow.getCgst());
                jsonObject.put("sgst", tRow.getSgst());
                jsonObject.put("total_igst", tRow.getTotalIgst());
                jsonObject.put("total_sgst", tRow.getTotalSgst());
                jsonObject.put("total_cgst", tRow.getTotalCgst());
                jsonObject.put("final_amt", tRow.getFinalAmt());
                jsonObject.put("is_batch", tRow.isBatch());
                jsonObject.put("b_details_id", tRow.getSelectedBatch().getId());
                jsonObject.put("b_no", tRow.getSelectedBatch().getBatchNo());
//                jsonObject.put("b_rate", tRow.getSelectedBatch().getbRate());
//                jsonObject.put("b_purchase_rate", tRow.getSelectedBatch().getPurchaseRate());
//                jsonObject.put("b_expiry", tRow.getSelectedBatch().getExpiryDate());
//                jsonObject.put("sales_rate", tRow.getSelectedBatch().getSalesRate());
//                jsonObject.put("rate_a", tRow.getSelectedBatch().getMinRateA());
//                jsonObject.put("rate_b", tRow.getSelectedBatch().getMinRateB());
//                jsonObject.put("rate_c", tRow.getSelectedBatch().getMinRateC());
//                jsonObject.put("min_margin", tRow.getSelectedBatch().getMinMargin());
//                jsonObject.put("margin_per", tRow.getSelectedBatch().getMinMargin());
//                jsonObject.put("manufacturing_date", tRow.getSelectedBatch().getManufacturingDate());
                jsonObject.put("isBatchNo", tRow.getSelectedBatch().getBatchNo());
                jsonObject.put("serialNo", tRow.getSerialNo());
                jsonObject.put("reference_type", tRow.getReferenceType());
                jsonObject.put("reference_id", tRow.getReferenceId());
                rowArr.put(jsonObject);
            }
        }
        boolean taxFlag = false;
        JSONObject taxCal = new JSONObject();
        if (cmbTranxSalesInvoiceCreateSupplierGST.getValue() != null && Integer.valueOf(cmbTranxSalesInvoiceCreateSupplierGST.getValue().getState()) != GlobalTranx.getCompanyStateCode()) {
            taxFlag = false;
            JSONArray jsonArrIgst = new JSONArray();
            for (TranxTaxCal tranxTaxCal : igstCal) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("gst", tranxTaxCal.getTax());
                jsonObject.put("amt", tranxTaxCal.getTaxAmt());
                jsonArrIgst.put(jsonObject);

            }
            taxCal.put("igst", jsonArrIgst);
        } else {
            taxFlag = true;
            JSONArray jsonArrCgst = new JSONArray();
            for (TranxTaxCal tranxTaxCal : cgstCal) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("gst", tranxTaxCal.getTax());
                jsonObject.put("amt", tranxTaxCal.getTaxAmt());
                jsonArrCgst.put(jsonObject);
            }
            JSONArray jsonArrSgst = new JSONArray();
            for (TranxTaxCal tranxTaxCal : sgstCal) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("gst", tranxTaxCal.getTax());
                jsonObject.put("amt", tranxTaxCal.getTaxAmt());
                jsonArrSgst.put(jsonObject);
            }
            taxCal.put("cgst", jsonArrCgst);
            taxCal.put("sgst", jsonArrSgst);
        }

        Double totalcgst = cgstCal.stream().mapToDouble(v -> v.getTaxAmt()).sum();
        Double totalsgst = sgstCal.stream().mapToDouble(v -> v.getTaxAmt()).sum();
        Double totaligst = igstCal.stream().mapToDouble(v -> v.getTaxAmt()).sum();

        JSONArray additionalCharges = new JSONArray();
        Map<String, String> body = new HashMap<>();
        body.put("bill_dt", DateConvertUtil.convertDatetoAPIDateString(dpTranxSalesInvoiceCreateInvoiceDate.getValue()));
        body.put("sales_invoice_id", selectedSalesInvoiceId.toString());
        body.put("sales_return_no", tfTranxSalesInvoiceCreateInvoiceNo.getText());
        body.put("sales_acc_id", String.valueOf(cmbTranxSalesInvoiceCreateSalesAcc.getValue().getId()));
        body.put("sales_return_sr_no", tfTranxSalesInvoiceCreateInvoiceSrNo.getText());
        body.put("gstNo", cmbTranxSalesInvoiceCreateSupplierGST.getValue() != null ? cmbTranxSalesInvoiceCreateSupplierGST.getValue().getGstNo() : "");
        body.put("isRoundOffCheck", String.valueOf(chkRoundOff.isSelected()));
        body.put("debtors_id", String.valueOf(ledgerId));
        body.put("roundoff", tfTranxRoundOff.getText());
        body.put("narration", tfTranxSaleInvCreateNarration.getText());
        body.put("totalamt", String.valueOf(tfTranxBillAmtTotal.getText()));
        body.put("totalcgst", String.valueOf(totalcgst));
        body.put("totalsgst", String.valueOf(totalsgst));
        body.put("totaligst", String.valueOf(totaligst));
        body.put("totalqty", String.valueOf(tfTranxTotalQty.getText()));
        body.put("tcs_per", String.valueOf(0.0));
        body.put("tcs_amt", String.valueOf(0.0));
        body.put("tcs_mode", "");
        body.put("sales_discount", String.valueOf(0.0));
        body.put("total_sales_discount_amt", String.valueOf(0.0));
        body.put("sales_discount_amt", String.valueOf(0.0));
        body.put("row", String.valueOf(rowArr));
        body.put("additionalChargesTotal", String.valueOf(0.0));
        body.put("additionalCharges", additionalCharges.toString());
        body.put("sale_type", "sales");
        if (cmbTranxSalesInvoiceCreateSupplierGST.getValue() != null) {
            body.put("gstType", cmbTranxSalesInvoiceCreateSupplierGST.getValue() != null ? cmbTranxSalesInvoiceCreateSupplierGST.getValue().getId() : "");
        }
        if (salesDiscLedger > -1) {
            body.put("sales_disc_ledger", salesDiscLedger.toString());
        }
        body.put("salesmanId", "");
        body.put("print_type", "");
        body.put("paymentMode", "");
        body.put("taxCalculation", taxCal.toString());
        body.put("taxFlag", String.valueOf(taxFlag));
        body.put("total_qty", String.valueOf(tfTranxTotalQty.getText()));
        body.put("total_free_qty", String.valueOf(tfTranxFreeQty.getText()));
        body.put("total_row_gross_amt", String.valueOf(tfTranxGrossAmtTotal.getText()));
        body.put("total_base_amt", String.valueOf(tfTranxGrossAmtTotal.getText()));
        body.put("total_invoice_dis_amt", String.valueOf(tfTranxDiscountTotal.getText()));
        body.put("taxable_amount", String.valueOf(tfTranxGrossAmtTotal.getText()));
        body.put("total_tax_amt", String.valueOf(tfTranxTaxTotal.getText()));
        body.put("bill_amount", String.valueOf(tfTranxBillAmtTotal.getText()));
        body.put("id", String.valueOf(editId));
//        System.out.println("body =>" + body);
        Map<String, String> headers = new HashMap<>();
//        headers.put("branch", "gvhm001");
        String response = APIClient.postMultipartRequest(body, null, EndPoints.TRANX_SALES_INVOICE_RTN_EDIT, headers);
        JSONObject resObj = new JSONObject(response);
        if (resObj.getInt("responseStatus") == 200) {
            AlertUtility.AlertSuccess(AlertUtility.alertTypeSuccess, resObj.getString("message"), input -> {
                GlobalController.getInstance().addTabStatic(SALES_INVOICE_RTN_CREATE_SLUG, false);
            });
        } else {
            AlertUtility.AlertError(AlertUtility.alertTypeError, resObj.getString("message"), input -> {
            });
        }
    }

    public void TranxCancel(ActionEvent actionEvent) {
        AlertUtility.AlertConfirmation(AlertUtility.alertTypeConfirmation, "Are you sure?", input -> {
            if (input == 1) GlobalController.getInstance().addTabStatic(SALES_INVOICE_RTN_CREATE_SLUG, false);
        });
    }

    public void TranxModify(ActionEvent actionEvent) {
        GlobalController.getInstance().addTabStatic(SALES_INVOICE_RTN_LIST_SLUG, false);
    }

    public void setEditId(Integer id) {
//        System.out.println("Invoice Id =>" + id);
        editId = id;
        Map<String, String> body = new HashMap<>();
        body.put("id", id.toString());
        Map<String, String> headers = new HashMap<>();
//        headers.put("branch", "gvhm001");
        String response = APIClient.postMultipartRequest(body, null, EndPoints.TRANX_SALES_INVOICE_RTN_EDIT_BY_ID, headers);
        JSONObject resObj = new JSONObject(response);
        if (resObj.getInt("responseStatus") == 200) {
            selectedSalesInvoiceId = resObj.getInt("invoice_id");
            salesDiscLedger = resObj.getInt("discountLedgerId");
            JSONObject invoiceData = resObj.getJSONObject("invoice_data");
            tfTranxSalesInvoiceCreateInvoiceNo.setText(invoiceData.getString("invoice_no"));
            tfTranxSalesInvoiceCreateInvoiceSrNo.setText(invoiceData.getInt("sales_sr_no") + "");
            dpTranxSalesInvoiceCreateInvoiceDate.setValue(DateConvertUtil.convertStringToLocalDateWithFormat(invoiceData.getString("transaction_dt")));
            ledgerId = "" + invoiceData.getInt("supplierId");
            ledgerName = invoiceData.getString("debtor_name");
            tfTranxSalesInvoiceCreateLedgerName.setText(ledgerName);
            chkRoundOff.setSelected(invoiceData.getBoolean("isRoundOffCheck"));
            tfTranxSaleInvCreateNarration.setText(invoiceData.getString("narration"));
            setRowData(resObj.getJSONArray("row"));
        }
    }


    private void setRowData(JSONArray row) {
        tblTranxSalesInvoiceCreateCMPTRow.getItems().clear();
        for (Object rowObj : row) {
            try {
                TranxRowRes currRowRes = new Gson().fromJson(rowObj.toString(), TranxRowRes.class);
                TranxSelectedProduct selectedProduct = TranxCommonPopUps.getSelectedProductFromProductId(currRowRes.getProductName());
                TranxRow currRow = GlobalTranx.newEmptyTranxRow();
                currRow.setDetailId(currRowRes.getDetailsId());
                currRow.setProductId(currRowRes.getProductId());
                currRow.setProductName(currRowRes.getProductName());
                UnitLevelLst unitLevelLst = GlobalTranx.getUnitsFromProductId(currRowRes.getProductId());
                currRow.setUnitLevelLst(unitLevelLst);
                unitLevelLst.getLevelAOpts().stream().filter((s) -> s.getValue().equalsIgnoreCase(String.valueOf(currRowRes.getLevelAId()))).findAny().ifPresent(p -> {
                    currRow.setSelectedLevelA(p);
                    currRow.setDispLevelA(p.getLabel());
                    currRow.setLevelAId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
                });
                currRow.getAllLevelBOpt().stream().filter((s) -> s.getValue().equalsIgnoreCase(String.valueOf(currRowRes.getLevelBId()))).findAny().ifPresent(p -> {
                    currRow.setSelectedLevelB(p);
                    currRow.setDispLevelB(p.getLabel());
                    currRow.setLevelBId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
                });
                currRow.getAllLevelCOpt().stream().filter((s) -> s.getValue().equalsIgnoreCase(String.valueOf(currRowRes.getLevelCId()))).findAny().ifPresent(p -> {
                    currRow.setSelectedLevelC(p);
                    currRow.setDispLevelC(p.getLabel());
                    currRow.setLevelCId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
                });
                currRow.getAllUnitOpt().stream().filter((s) -> s.getValue().equalsIgnoreCase(String.valueOf(currRowRes.getUnitId()))).findAny().ifPresent(p -> {
                    currRow.setSelectedUnit(p);
                    currRow.setDispUnit(p.getLabel());
                    currRow.setUnitId(!p.getValue().trim().isEmpty() ? Integer.valueOf(p.getValue()) : 0);
                    currRow.setUnitConv(p.getUnitConversion());
                });
                currRow.setSelectedProduct(selectedProduct);
                currRow.setQty(currRowRes.getQty());
                currRow.setRate(currRowRes.getRate());
                currRow.setBatch(currRowRes.getBatch());
                currRow.setDisPer(currRowRes.getDisPer());
                currRow.setDisPer2(currRowRes.getDisPer2());
                currRow.setDisAmt(currRowRes.getDisAmt());
                currRow.setProductPackage(currRowRes.getPackName());
                currRow.setDispUnit(currRowRes.getUnitName());
                currRow.setbNo(currRowRes.getBatchNo());
                currRow.setIgst(currRowRes.getIgst());
                currRow.setSgst(currRowRes.getSgst());
                currRow.setCgst(currRowRes.getCgst());
                TranxSelectedBatch selectedBatch = TranxCommonPopUps.getSelectedBatchFromProductId(currRow, dpTranxSalesInvoiceCreateInvoiceDate.getValue().toString(), currRowRes.getBatchNo());
                currRow.setSelectedBatch(selectedBatch);
                tblTranxSalesInvoiceCreateCMPTRow.getItems().add(currRow);
            } catch (Exception e) {
                e.printStackTrace();
                loggerTranxSalesInvoiceEdit.error("Exception in setRowData() :" + Globals.getExceptionString(e));
            }
        }
        TranxCal();
    }

    public void onClickAdditionalCharges(ActionEvent actionEvent) {
        Stage stage = (Stage) rootAnchorPane.getScene().getWindow();

        AdditionalCharges.additionalCharges(stage, addChargesDTOList, Double.valueOf(tfTranxGrossAmtTotal.getText()), (input1, input2) -> {
            addChargesDTOList.clear();
            addChargesDTOList.addAll(input2);
//            System.out.println("acDelDetailsIds:"+input1[0]);
//            System.out.println("Add Charges:"+input1[1]);
//            System.out.println(addChargesDTOList);
            tfTranxAdditionalTotal.setText(input1[1]);
            TranxCal();
        });
    }


    private void initGstTable() {
        igstCal = new ArrayList<>();
        cgstCal = new ArrayList<>();
        sgstCal = new ArrayList<>();
        tc_digst.setCellValueFactory(new PropertyValueFactory<>("digst"));
        tc_igst.setCellValueFactory(new PropertyValueFactory<>("igst"));
        tc_dgst.setCellValueFactory(new PropertyValueFactory<>("dgst"));
        tc_cgst.setCellValueFactory(new PropertyValueFactory<>("cgst"));
        tc_sgst.setCellValueFactory(new PropertyValueFactory<>("sgst"));
        gstTableColumnVisible(true);
        TranxCalGst();
    }

    private void TranxCalGst() {
        ObservableList<TranxGst> lstGst = FXCollections.observableArrayList();
        if (igstCal.size() > 0) {
            for (int i = 0; i < igstCal.size(); i++) {
                lstGst.add(new TranxGst(igstCal.get(i).getTax(), igstCal.get(i).getTaxAmt(), cgstCal.get(i).getTax(), cgstCal.get(i).getTaxAmt(), sgstCal.get(i).getTaxAmt()));
            }
        }
        tvGST_Table.setItems(lstGst);
        if (cmbTranxSalesInvoiceCreateSupplierGST.getValue() != null && Integer.valueOf(cmbTranxSalesInvoiceCreateSupplierGST.getValue().getState()) != GlobalTranx.getCompanyStateCode()) {
            //? Igst Calculate
            gstTableColumnVisible(false);
        } else {
            //? Cgst Calculate
            gstTableColumnVisible(true);
        }

    }

    private void gstTableColumnVisible(Boolean isCgstVisible) {
        if (isCgstVisible) {
            tc_igst.setVisible(false);
            tc_digst.setVisible(false);
            tc_dgst.setVisible(true);
            tc_cgst.setVisible(true);
            tc_sgst.setVisible(true);
        } else {
            tc_igst.setVisible(true);
            tc_digst.setVisible(true);
            tc_dgst.setVisible(false);
            tc_cgst.setVisible(false);
            tc_sgst.setVisible(false);
        }
    }


    //todo:Set the Ledger Details at the Bottom in ledger Tab
    private void tranxLedgerDetailsFun(String id) {
        APIClient apiClient = null;
        //todo: activating the ledger tab
        tpTranxBottom.getSelectionModel().select(tbTranxLedgerBottom);
        loggerTranxSalesInvoiceEdit.debug("Get Ledger Details Data Started...");
        Map<String, String> map = new HashMap<>();
        map.put("ledger_id", id);
        String formData = Globals.mapToStringforFormData(map);
//        HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.TRANSACTION_LEDGER_DETAILS);
        apiClient = new APIClient(EndPoints.TRANSACTION_LEDGER_DETAILS, formData, RequestType.FORM_DATA);

        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);

                if (jsonObject.get("responseStatus").getAsInt() == 200) {
                    JsonObject jsonObj = jsonObject.getAsJsonObject("result");
                    //todo: getting values
                    String gstNumber = jsonObj.get("gst_number").getAsString() != null ? jsonObj.get("gst_number").getAsString() : "";
                    String area = jsonObj.get("area").getAsString() != null ? jsonObj.get("area").getAsString() : "";
                    String bankName = jsonObj.get("bank_name").getAsString() != null ? jsonObj.get("bank_name").getAsString() : "";
                    String contactName = jsonObj.get("contact_name").getAsString() != null ? jsonObj.get("contact_name").getAsString() : "";
                    String creditDays = jsonObj.get("credit_days").getAsString() != null ? jsonObj.get("credit_days").getAsString() : "";
                    String fssaiNumber = jsonObj.get("fssai_number").getAsString() != null ? jsonObj.get("fssai_number").getAsString() : "";
                    String licenseNumber = jsonObj.get("license_number").getAsString() != null ? jsonObj.get("license_number").getAsString() : "";
                    String route = jsonObj.get("route").getAsString() != null ? jsonObj.get("route").getAsString() : "";
//            String transport = jsonObj.get("route").getAsString();

                    //todo: setting values
                    lbTranxLedgerBottomGst.setText(gstNumber);
                    lbTranxLedgerBottomArea.setText(area);
                    lbTranxLedgerBottomBank.setText(bankName);
                    lbTranxLedgerBottomContactPerson.setText(contactName);
//            txtPurOrderTransportName.setText(jsonObject.get("contact_name").getAsString());
                    lbTranxLedgerBottomCreditDays.setText(creditDays);
                    lbTranxLedgerBottomFSSAI.setText(fssaiNumber);
                    lbTranxLedgerBottomLicenseNo.setText(licenseNumber);
                    lbTranxLedgerBottomRoute.setText(route);
                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API cancelled in tranxLedgerDetailsFun()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API failed in tranxLedgerDetailsFun()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        loggerTranxSalesInvoiceEdit.debug("Get Ledger Details Data End...");

    }

    //todo:Set the Product Details at the Bottom in Product Tab
    private void tranxProductDetailsFun(String id) {
        APIClient apiClient = null;
        //todo: activating the product tab
        tpTranxBottom.getSelectionModel().select(tbTranxProductBottom);
        loggerTranxSalesInvoiceEdit.debug("Get Product Details Data Started...");
        Map<String, String> map = new HashMap<>();
        map.put("product_id", id);
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("FormData: " + formData);
//        HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.TRANSACTION_PRODUCT_DETAILS);
        apiClient = new APIClient(EndPoints.TRANSACTION_PRODUCT_DETAILS, formData, RequestType.FORM_DATA);

        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                String responseBody = response.body();
//                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
//                System.out.println("tranxProductDetailsFun Res ==>" + jsonObject);
                if (jsonObject.get("responseStatus").getAsInt() == 200) {
                    JsonObject productJsonObj = jsonObject.getAsJsonObject("result");
                    //todo: getting values
                    String brand = productJsonObj.get("brand").getAsString();
                    String group = productJsonObj.get("group").getAsString();
                    String subGroup = productJsonObj.get("subgroup").getAsString();
                    String category = productJsonObj.get("category").getAsString();
                    String hsn = productJsonObj.get("hsn").getAsString();
                    String taxType = productJsonObj.get("tax_type").getAsString();
                    String taxPer = productJsonObj.get("tax_per").getAsString();
                    String marginPer = productJsonObj.get("margin_per").getAsString();
                    String cost = productJsonObj.get("cost").getAsString();
                    String shelfId = productJsonObj.get("shelf_id").getAsString();
                    String minStock = productJsonObj.get("min_stocks").getAsString();
                    String maxStock = productJsonObj.get("max_stocks").getAsString();

                    //todo: setting values
                    lbTranxProductBottomBrand.setText(brand);
                    lbTranxProductBottomGroup.setText(group);
                    lbTranxProductBottomSubGroup.setText(subGroup);
                    lbTranxProductBottomCategory.setText(category);
                    lbTranxProductBottomHSN.setText(hsn);
                    lbTranxProductBottomTaxType.setText(taxType);
                    lbTranxProductBottomTaxPer.setText(taxPer);
                    lbTranxProductBottomMarginPer.setText(marginPer);
                    lbTranxProductBottomCost.setText(cost);
                    lbTranxProductBottomShelfId.setText(shelfId);
                    lbTranxProductBottomMinStock.setText(minStock);
                    lbTranxProductBottomMaxStock.setText(maxStock);
                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API cancelled in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API failed in tranxProductDetailsFun()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        loggerTranxSalesInvoiceEdit.debug("Get Product Details Data End...");

    }

    //todo:Set the SUpplier Details of the Selected Product Bottom in Supplier Table
    private void getSupplierListbyProductId(String id) {
        APIClient apiClient = null;
        loggerTranxSalesInvoiceEdit.debug("Get Supplier List Data Started...");
        Map<String, String> map = new HashMap<>();
        map.put("productId", id);
        String formData = Globals.mapToStringforFormData(map);
        System.out.println("FormData: " + formData);
//        HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.GET_SUPPLIERLIST_BY_PRODUCTID_ENDPOINT);
        apiClient = new APIClient(EndPoints.GET_SUPPLIERLIST_BY_PRODUCTID_ENDPOINT, formData, RequestType.FORM_DATA);

        apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                JsonObject jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//                String responseBody = response.body();
//                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                if (jsonObject.get("responseStatus").getAsInt() == 200) {
//            JsonObject resultObj = jsonObject.getAsJsonObject("result");
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");
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
                    cmSupplierName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
                    cmInvNo.setCellValueFactory(new PropertyValueFactory<>("supplierInvNo"));
                    cmInvDate.setCellValueFactory(new PropertyValueFactory<>("supplierInvDate"));
                    cmBatch.setCellValueFactory(new PropertyValueFactory<>("supplierBatch"));
                    cmMRP.setCellValueFactory(new PropertyValueFactory<>("supplierMrp"));
                    cmQty.setCellValueFactory(new PropertyValueFactory<>("supplierQty"));
                    cmRate.setCellValueFactory(new PropertyValueFactory<>("supplierRate"));
                    cmCost.setCellValueFactory(new PropertyValueFactory<>("supplierCost"));
                    cmDisPer.setCellValueFactory(new PropertyValueFactory<>("supplierDisPer"));
                    cmDisAmt.setCellValueFactory(new PropertyValueFactory<>("supplierDisAmt"));

                    tvInvoiceProductHistory.setItems(supplierDataList);

                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API cancelled in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API failed in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        loggerTranxSalesInvoiceEdit.debug("Get Supplier List Data End...");

    }    //todo:Set the SUpplier Details of the Selected Product Bottom in Supplier Table

    private void getBatchDetailsByBatchNoAndId(String bNo, String bId) {
        APIClient apiClient = null;
        tpTranxBottom.getSelectionModel().select(tbTranxBatchBottom);
        loggerTranxSalesInvoiceEdit.debug("Get Supplier List Data Started...");
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

                    lbTranxBatchBottomBNo.setText(batchJsonObj.get("batchNo").getAsString());
                    lbTranxBatchBottomMRP.setText(batchJsonObj.get("batchMrp").getAsString());
                    lbTranxBatchBottomMRP.setText(batchJsonObj.get("batchMrp").getAsString());
                    lbTranxBatchBottomTotalAmt.setText(batchJsonObj.get("net_rate").getAsString());
                    lbTranxBatchBottomMFGDate.setText(DateConvertUtil.convertDispDateFormat(batchJsonObj.get("mfgDate").getAsString()));
                    lbTranxBatchBottomPurRate.setText(batchJsonObj.get("pur_rate").getAsString());
                    lbTranxBatchBottomMinRateA.setText(batchJsonObj.get("min_rate_a").getAsString());
                    lbTranxBatchBottomAvailableSt.setText(batchJsonObj.get("qty").getAsString());
                    lbTranxBatchBottomDiscAmt.setText(batchJsonObj.get("b_dis_amt").getAsString());
                    lbTranxBatchBottomMarginPer.setText(batchJsonObj.get("b_dis_amt").getAsString());
                    lbTranxBatchBottomSalesRate.setText(batchJsonObj.get("sales_rate").getAsString());
                    lbTranxBatchBottomCessPer.setText("");
                    lbTranxBatchBottomCessAmt.setText("");

                }
            }
        });
        apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API cancelled in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());

            }
        });

        apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                loggerTranxSalesInvoiceEdit.error("Network API failed in getSupplierListbyProductId()" + workerStateEvent.getSource().getValue().toString());
            }
        });
        apiClient.start();
        loggerTranxSalesInvoiceEdit.debug("Get Supplier List Data End...");
    }


}
