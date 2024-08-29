package com.opethic.genivis.controller.GSTR2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.controller.Reports.AccountsLedgerReport2Controller;
import com.opethic.genivis.controller.Reports.StocksStockReport2Controller;
import com.opethic.genivis.dto.GSTR2.GSTR2B2BOtherTaxableInvoiceDTO;
import com.opethic.genivis.dto.GSTR2.GSTR2CrDrRegisterDTO;
import com.opethic.genivis.dto.LedgerReport1DTO;
import com.opethic.genivis.dto.StockReport1DTO;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.*;

public class GSTR2B2BOtherTaxableInvoiceController implements Initializable {
    private static final Logger GSTR2B2BOtherTaxableInvoiceListLogger = LoggerFactory.getLogger(GSTR2B2BOtherTaxableInvoiceController.class);
    @FXML
    private TableView<GSTR2B2BOtherTaxableInvoiceDTO> tblvGSTR2B2BOtherTaxableInvoiceList;
    @FXML
    private TableColumn<GSTR2B2BOtherTaxableInvoiceDTO, String> tblGSTR2B2BOtherTaxableInvoiceSrNo, tblGSTR2B2BOtherTaxableInvoiceParticulars,
            tblGSTR2B2BOtherTaxableInvoiceGSTIN, tblGSTR2B2BOtherTaxableInvoiceVoucherType, tblGSTR2B2BOtherTaxableInvoiceTaxableAmt,
            tblGSTR2B2BOtherTaxableInvoiceIGSTAmt, tblGSTR2B2BOtherTaxableInvoiceCGSTAmt, tblGSTR2B2BOtherTaxableInvoiceSGSTAmt,
            tblGSTR2B2BOtherTaxableInvoiceCessAmt, tblGSTR2B2BOtherTaxableInvoiceTaxAmt, tblGSTR2B2BOtherTaxableInvoiceInvoiceAmt;
    private ObservableList<GSTR2B2BOtherTaxableInvoiceDTO> originalData;
    @FXML
    private BorderPane mainBorderpane;
    @FXML
    private TextField tfGSTR2B2BOtherTaxableInvoiceSearch, tfStartDt, tfEndDt;
    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private Label lblTotalTaxable_amt, lblTotalIgst_amt, lblTotalCgst_amt, lblTotalSgst_amt, lblTotalTax_amt, lblTotalInvoice_amt;

    private JsonObject jsonObject = null;

    String Start_date = "", End_date = "";

    public void initialize(URL url, ResourceBundle resourceBundle) {

        //TODO: resizing the table columns as per the resolution.
        tblvGSTR2B2BOtherTaxableInvoiceList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        responsiveTableDesign();

        Platform.runLater(() -> tfGSTR2B2BOtherTaxableInvoiceSearch.requestFocus());

        mainBorderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR2_DASHBOARD_LIST_SLUG, false);
            }
        });

        GSTR2B2BOtherTaxableInvoiceList("");

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                GSTR2B2BOtherTaxableInvoiceList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR2B2BOtherTaxableInvoiceSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        mainBorderpane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.DOWN && tfGSTR2B2BOtherTaxableInvoiceSearch.isFocused()){
                tblvGSTR2B2BOtherTaxableInvoiceList.getSelectionModel().select(0);
                tblvGSTR2B2BOtherTaxableInvoiceList.requestFocus();
            }
        });

        tblvGSTR2B2BOtherTaxableInvoiceList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                GSTR2B2BOtherTaxableInvoiceDTO selectedItem = (GSTR2B2BOtherTaxableInvoiceDTO) tblvGSTR2B2BOtherTaxableInvoiceList.getSelectionModel().getSelectedItem();
                String id = selectedItem.getId();
                String startDate = Communicator.text_to_date.fromString(tfStartDt.getText()).toString();
                String endDate = Communicator.text_to_date.fromString(tfEndDt.getText()).toString();
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceId = id;
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceStartDate = startDate;
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceEndDate = endDate;

                GlobalController.getInstance().addTabStatic(GSTR2_B2B_Other_Taxable_Invoice_2_LIST_SLUG, false);
            }
        });

        tblvGSTR2B2BOtherTaxableInvoiceList.addEventFilter(KeyEvent.ANY, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                GSTR2B2BOtherTaxableInvoiceDTO selectedItem = (GSTR2B2BOtherTaxableInvoiceDTO) tblvGSTR2B2BOtherTaxableInvoiceList.getSelectionModel().getSelectedItem();
                String id = selectedItem.getId();
                String startDate = Communicator.text_to_date.fromString(tfStartDt.getText()).toString();
                String endDate = Communicator.text_to_date.fromString(tfEndDt.getText()).toString();
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceId = id;
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceStartDate = startDate;
                GSTR2B2BOtherTaxableInvoice2Controller.GSTR2B2BOtherTaxableInvoiceEndDate = endDate;

                GlobalController.getInstance().addTabStatic(GSTR2_B2B_Other_Taxable_Invoice_2_LIST_SLUG, false);
            }
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfGSTR2B2BOtherTaxableInvoiceSearch);
        setKeyPressedHandler(tfStartDt);
        setKeyPressedHandler(tfEndDt);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvGSTR2B2BOtherTaxableInvoiceList.requestFocus();

        tfEndDt.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.ENTER){
                GSTR2B2BOtherTaxableInvoiceList("");
                Node nextNode = CommonTraversa.getNextFocusableNode(btExportPdf.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if(event.getCode() == KeyCode.TAB){
                GSTR2B2BOtherTaxableInvoiceList("");
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
        tblGSTR2B2BOtherTaxableInvoiceSrNo.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.05));
        tblGSTR2B2BOtherTaxableInvoiceParticulars.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.23));
        tblGSTR2B2BOtherTaxableInvoiceGSTIN.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceVoucherType.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceTaxableAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceIGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceCGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceSGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceCessAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceTaxAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoiceInvoiceAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoiceList.widthProperty().multiply(0.08));
    }

    private void GSTR2B2BOtherTaxableInvoiceList(String searchKey) {
        GSTR2B2BOtherTaxableInvoiceListLogger.info("Fetch GSTR2 B2B Other Taxable Invoice List  : GSTR2B2BOtherTaxableInvoiceController");
        APIClient apiClient = null;
        try {
            GSTR2B2BOtherTaxableInvoiceListLogger.debug("Get GSTR2 B2B Other Taxable Invoice Data Started...");
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
            apiClient = new APIClient(EndPoints.GSTR2_B2B_Other_Taxable, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<GSTR2B2BOtherTaxableInvoiceDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
//                        System.out.println("GSTR2CreditDebitRegisterList---> " + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);
//                        System.out.println(startLocalDt + " <-start end-> " + endLocalDt);
                        int countSr = 1;
                        if (responseList.size() > 0) {
                            tblvGSTR2B2BOtherTaxableInvoiceList.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("ledger_id") != null ? item.get("ledger_id").getAsString() : "";
                                String particulars = item.get("ledger_name") != null ? item.get("ledger_name").getAsString() : "";
                                String gstin_uin = item.get("gst_number") != null ? item.get("gst_number").getAsString() : "";
                                String voucher_count = item.get("total_invoices") != null ? item.get("total_invoices").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("total_igst") != null ? item.get("total_igst").getAsString() : "";
                                String cgst_amt = item.get("total_cgst") != null ? item.get("total_cgst").getAsString() : "";
                                String sgst_amt = item.get("total_sgst") != null ? item.get("total_sgst").getAsString() : "";
                                String cess_amt = item.get("") != null ? item.get("").getAsString() : "";
                                String tax_amt = item.get("total_tax") != null ? item.get("total_tax").getAsString() : "";
                                String invoice_amt = item.get("total_amt") != null ? item.get("total_amt").getAsString() : "";

                                observableList.add(new GSTR2B2BOtherTaxableInvoiceDTO(String.valueOf(countSr++), id, particulars, gstin_uin, voucher_count, taxable_amt, igst_amt, cgst_amt, sgst_amt, cess_amt, tax_amt, invoice_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblGSTR2B2BOtherTaxableInvoiceSrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tblGSTR2B2BOtherTaxableInvoiceParticulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblGSTR2B2BOtherTaxableInvoiceGSTIN.setCellValueFactory(new PropertyValueFactory<>("gstin_uin"));
                            tblGSTR2B2BOtherTaxableInvoiceVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucher_count"));
                            tblGSTR2B2BOtherTaxableInvoiceTaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceIGSTAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceCGSTAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceSGSTAmt.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceCessAmt.setCellValueFactory(new PropertyValueFactory<>("cess_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceTaxAmt.setCellValueFactory(new PropertyValueFactory<>("tax_amt"));
                            tblGSTR2B2BOtherTaxableInvoiceInvoiceAmt.setCellValueFactory(new PropertyValueFactory<>("invoice_amt"));

                            tblvGSTR2B2BOtherTaxableInvoiceList.setItems(observableList);

                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR2B2BOtherTaxableInvoiceListLogger.debug("Success in Displaying GSTR2 B2B Other Taxable Invoice List : GSTR2B2BOtherTaxableInvoiceController");

                        } else {
                            GSTR2B2BOtherTaxableInvoiceListLogger.debug("GSTR2 B2B Other Taxable Invoice List ResponseObject is null : GSTR2B2BOtherTaxableInvoiceController");
                            tblvGSTR2B2BOtherTaxableInvoiceList.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR2B2BOtherTaxableInvoiceListLogger.debug("Error in response of GSTR2 B2B Other Taxable Invoice List : GSTR2B2BOtherTaxableInvoiceController");
                        tblvGSTR2B2BOtherTaxableInvoiceList.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });

            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2B2BOtherTaxableInvoiceListLogger.error("Network API cancelled in GSTR2B2BOtherTaxableInvoiceList()" + workerStateEvent.getSource().getValue().toString());
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2B2BOtherTaxableInvoiceListLogger.error("Network API failed in GSTR2B2BOtherTaxableInvoiceList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR2B2BOtherTaxableInvoiceListLogger.debug("Get GSTR2 B2B Other Taxable Invoice Data End...");

        } catch (Exception e) {
            GSTR2B2BOtherTaxableInvoiceListLogger.error("GSTR2 B2B Other Taxable Invoice List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }

    }

    private void filterData(String keyword) {
        ObservableList<GSTR2B2BOtherTaxableInvoiceDTO> filteredData = FXCollections.observableArrayList();

        GSTR2B2BOtherTaxableInvoiceListLogger.error("Search GSTR2 B2B Other Taxable Invoice in GSTR2B2BOtherTaxableInvoiceController");
        if (keyword.isEmpty()) {
            tblvGSTR2B2BOtherTaxableInvoiceList.setItems(originalData);
            return;
        }

        for (GSTR2B2BOtherTaxableInvoiceDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR2B2BOtherTaxableInvoiceList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR2B2BOtherTaxableInvoiceDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
            item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
            item.getGstin_uin().toLowerCase().contains(lowerCaseKeyword) ||
            item.getVoucher_count().toLowerCase().contains(lowerCaseKeyword) ||
            item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getCess_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getTax_amt().toLowerCase().contains(lowerCaseKeyword) ||
            item.getInvoice_amt().toLowerCase().contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        ObservableList<GSTR2B2BOtherTaxableInvoiceDTO> filteredData = tblvGSTR2B2BOtherTaxableInvoiceList.getItems();
        // Calculate the Totals
        double totalTaxable_amt = 0.0;
        double totalIgst_amt = 0.0;
        double totalCgst_amt = 0.0;
        double totalSgst_amt = 0.0;
        double totalTax_amt = 0.0;
        double totalInvoice_amt = 0.0;

        for (GSTR2B2BOtherTaxableInvoiceDTO item : filteredData) {
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
