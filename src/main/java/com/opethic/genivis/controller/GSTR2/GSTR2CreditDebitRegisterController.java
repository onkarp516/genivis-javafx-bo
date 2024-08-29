package com.opethic.genivis.controller.GSTR2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR2.GSTR2CrDrRegisterDTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.CommonTraversa;
import com.opethic.genivis.utils.DateValidator;
import com.opethic.genivis.utils.GlobalController;
import com.opethic.genivis.utils.Globals;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.GSTR2_DASHBOARD_LIST_SLUG;

public class GSTR2CreditDebitRegisterController implements Initializable  {
    private static final Logger gstr2RegLogger = LoggerFactory.getLogger(GSTR2CreditDebitRegisterController.class);

    @FXML
    private TextField tfGSTR2CrDrRegLedgerSearch, tfStartDt, tfEndDt;
    @FXML
    private TableView<GSTR2CrDrRegisterDTO> tblvGSTR2CrDrRegList;
    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private Label lblTotalTaxable_amt, lblTotalIgst_amt, lblTotalCgst_amt, lblTotalSgst_amt, lblTotalTax_amt, lblTotalInvoice_amt;
    @FXML
    private TableColumn<GSTR2CrDrRegisterDTO, String> tblcGSTR2CrDrRegSrNo, tblcGSTR2CrDrRegDate, tblcGSTR2CrDrRegInvoiceNo,
            tblcGSTR2CrDrRegParticulars, tblcGSTR2CrDrRegVoucherType, tblcGSTR2CrDrRegTaxableAmt, tblcGSTR2CrDrRegIntegratedTaxAmt,
            tblcGSTR2CrDrRegCentralTaxAmt, tblcGSTR2CrDrRegStateTaxAmt, tblcGSTR2CrDrRegCessAmt, tblcGSTR2CrDrRegTaxAmt, tblcGSTR2CrDrRegInvoiceAmt;
    private ObservableList<GSTR2CrDrRegisterDTO> originalData;
    @FXML
    private BorderPane mainBorderpane;

    private JsonObject jsonObject = null;

    String Start_date = "", End_date = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //TODO: resizing the table columns as per the resolution.
        tblvGSTR2CrDrRegList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        responsiveTableDesign();

        Platform.runLater(() -> tfGSTR2CrDrRegLedgerSearch.requestFocus());

        mainBorderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR2_DASHBOARD_LIST_SLUG, false);
            }
        });

        GSTR2CreditDebitRegisterList("");

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                GSTR2CreditDebitRegisterList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR2CrDrRegLedgerSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfGSTR2CrDrRegLedgerSearch);
        setKeyPressedHandler(tfStartDt);
        setKeyPressedHandler(tfEndDt);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvGSTR2CrDrRegList.requestFocus();

        tfEndDt.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.ENTER){
                GSTR2CreditDebitRegisterList("");
                Node nextNode = CommonTraversa.getNextFocusableNode(btExportPdf.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if(event.getCode() == KeyCode.TAB){
                GSTR2CreditDebitRegisterList(""); 
            }
        });
    }

    // Method to handle key pressed event for any Node
    private void setKeyPressedHandler(Node node) {
        node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Node nextNode = CommonTraversa.getNextFocusableNode(node.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
        });
    }

    private void responsiveTableDesign() {
        tblcGSTR2CrDrRegSrNo.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.04));
        tblcGSTR2CrDrRegDate.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.06));
        tblcGSTR2CrDrRegInvoiceNo.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.1));
        tblcGSTR2CrDrRegParticulars.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.15));
        tblcGSTR2CrDrRegVoucherType.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.1));
        tblcGSTR2CrDrRegTaxableAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.08));
        tblcGSTR2CrDrRegIntegratedTaxAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.07));
        tblcGSTR2CrDrRegCentralTaxAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.1));
        tblcGSTR2CrDrRegStateTaxAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.1));
        tblcGSTR2CrDrRegCessAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.05));
        tblcGSTR2CrDrRegTaxAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.05));
        tblcGSTR2CrDrRegInvoiceAmt.prefWidthProperty().bind(tblvGSTR2CrDrRegList.widthProperty().multiply(0.1));
    }

    private void GSTR2CreditDebitRegisterList(String searchKey) {
        gstr2RegLogger.info("Fetch GSTR2 Credit Debit Register List  : GSTR2CreditDebitRegisterController");
        APIClient apiClient = null;
        try {
            gstr2RegLogger.debug("Get GSTR2 Credit Debit Register Data Started...");
            Map<String, String> body = new HashMap<>();
            body.put("searchText", "");
//            body.put("start_date", "");
//            body.put("end_date", "");
            if (!tfStartDt.getText().equalsIgnoreCase("") || !tfEndDt.getText().equalsIgnoreCase("")) {
//                System.out.println("date sdf " );
                body.put("start_date", Communicator.text_to_date.fromString(tfStartDt.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfEndDt.getText()).toString());
            }
//            System.out.println("body ----------->  " + body);
            String requestBody = Globals.mapToStringforFormData(body);
            apiClient = new APIClient(EndPoints.GSTR2_CREDIT_DEBIT_Register, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<GSTR2CrDrRegisterDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
//                        System.out.println("GSTR2CreditDebitRegisterList---> " + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);
                        int countSr = 1;
                        if (responseList.size() > 0){
                            tblvGSTR2CrDrRegList.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id") != null ? item.get("id").getAsString() : "";
                                String dates = item.get("transaction_date") != null ? item.get("transaction_date").getAsString() : "";
                                String invoice_no = item.get("invoice_no") != null ? item.get("invoice_no").getAsString() : "";
                                String particulars = item.get("ledger_name") != null ? item.get("ledger_name").getAsString() : "";
                                String voucher_type = item.get("voucher_type") != null ? item.get("voucher_type").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("total_igst") != null ? item.get("total_igst").getAsString() : "";
                                String cgst_amt = item.get("total_cgst") != null ? item.get("total_cgst").getAsString() : "";
                                String sgst_amt = item.get("total_sgst") != null ? item.get("total_sgst").getAsString() : "";
                                String cess_amt = item.get("") != null ? item.get("").getAsString() : "";
                                String tax_amt = item.get("total_tax") != null ? item.get("total_tax").getAsString() : "";
                                String invoice_amt = item.get("total_amt") != null ? item.get("total_amt").getAsString() : "";

                                observableList.add(new GSTR2CrDrRegisterDTO(String.valueOf(countSr++), id, dates, invoice_no, particulars, voucher_type, taxable_amt, igst_amt, cgst_amt, sgst_amt, cess_amt, tax_amt, invoice_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblcGSTR2CrDrRegSrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tblcGSTR2CrDrRegDate.setCellValueFactory(new PropertyValueFactory<>("dates"));
                            tblcGSTR2CrDrRegInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoice_no"));
                            tblcGSTR2CrDrRegParticulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblcGSTR2CrDrRegVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                            tblcGSTR2CrDrRegTaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblcGSTR2CrDrRegIntegratedTaxAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblcGSTR2CrDrRegCentralTaxAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblcGSTR2CrDrRegStateTaxAmt.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblcGSTR2CrDrRegCessAmt.setCellValueFactory(new PropertyValueFactory<>("cess_amt"));
                            tblcGSTR2CrDrRegTaxAmt.setCellValueFactory(new PropertyValueFactory<>("tax_amt"));
                            tblcGSTR2CrDrRegInvoiceAmt.setCellValueFactory(new PropertyValueFactory<>("invoice_amt"));


                            tblvGSTR2CrDrRegList.setItems(observableList);

                            originalData = observableList;
                            calculateTotalAmount();
                            gstr2RegLogger.debug("Success in Displaying GSTR2 Credit Debit Unregister List : GSTR2CreditDebitUnregisterController");

                        } else {
                            gstr2RegLogger.debug("GSTR2 Credit Debit Register List ResponseObject is null : GSTR2CreditDebitRegisterController");
                            tblvGSTR2CrDrRegList.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        gstr2RegLogger.debug("Error in response of GSTR2 Credit Debit Register List : GSTR2CreditDebitRegisterController");
                        tblvGSTR2CrDrRegList.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });

            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    gstr2RegLogger.error("Network API cancelled in GSTR2CreditDebitRegisterList()" + workerStateEvent.getSource().getValue().toString());
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    gstr2RegLogger.error("Network API failed in GSTR2CreditDebitRegisterList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            gstr2RegLogger.debug("Get GSTR2 Credit Debit Register Data End...");

        } catch (Exception e) {
            gstr2RegLogger.error("GSTR2 Credit Debit Register List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    private void filterData(String keyword) {
        ObservableList<GSTR2CrDrRegisterDTO> filteredData = FXCollections.observableArrayList();

        gstr2RegLogger.error("Search GSTR2 Credit Debit Register in GSTR2CreditDebitRegisterController");
        if (keyword.isEmpty()) {
            tblvGSTR2CrDrRegList.setItems(originalData);
            return;
        }

        for (GSTR2CrDrRegisterDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR2CrDrRegList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR2CrDrRegisterDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getDates().toLowerCase().contains(lowerCaseKeyword) ||
                item.getInvoice_no().toLowerCase().contains(lowerCaseKeyword) ||
                item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCess_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTax_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getInvoice_amt().toLowerCase().contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount(){
        ObservableList<GSTR2CrDrRegisterDTO> filteredData = tblvGSTR2CrDrRegList.getItems();
        // Calculate the Totals
        double totalTaxable_amt = 0.0;
        double totalIgst_amt = 0.0;
        double totalCgst_amt = 0.0;
        double totalSgst_amt = 0.0;
        double totalTax_amt = 0.0;
        double totalInvoice_amt = 0.0;

        for (GSTR2CrDrRegisterDTO item : filteredData) {
            totalTaxable_amt += Double.parseDouble(item.getTaxable_amt().isEmpty() ? "0.0" : item.getTaxable_amt());
            totalIgst_amt += Double.parseDouble(item.getIgst_amt().isEmpty() ? "0.0" : item.getIgst_amt());
            totalCgst_amt += Double.parseDouble(item.getCgst_amt().isEmpty() ? "0.0" : item.getCgst_amt());
            totalSgst_amt += Double.parseDouble(item.getSgst_amt().isEmpty() ? "0.0" : item.getSgst_amt());
            totalTax_amt += Double.parseDouble(item.getTax_amt().isEmpty() ? "0.0" : item.getTax_amt());
            totalInvoice_amt += Double.parseDouble(item.getInvoice_amt().isEmpty() ? "0.0" : item.getInvoice_amt());
        }

        // Update Labels in the FXML
        lblTotalTaxable_amt.setText(String.format("%.2f", totalTaxable_amt));
        lblTotalIgst_amt.setText(String.format("%.2f", totalIgst_amt));
        lblTotalCgst_amt.setText(String.format("%.2f", totalCgst_amt));
        lblTotalSgst_amt.setText(String.format("%.2f", totalSgst_amt));
        lblTotalTax_amt.setText(String.format("%.2f", totalTax_amt));
        lblTotalInvoice_amt.setText(String.format("%.2f", totalInvoice_amt));
    }
}
