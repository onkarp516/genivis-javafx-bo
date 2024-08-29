package com.opethic.genivis.controller.GSTR1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR1.GSTR1CreditDebitRegisteredDTO;
import com.opethic.genivis.dto.LedgerReport1DTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
import com.opethic.genivis.utils.DateConvertUtil;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.GSTR1_DASHBOARD_LIST_SLUG;

public class GSTR1CreditDebitRegisteredController implements Initializable {
    private static final Logger GSTR1CreditDebitRegisteredListLogger = LoggerFactory.getLogger(GSTR1CreditDebitRegisteredController.class);
    @FXML
    private TableView<GSTR1CreditDebitRegisteredDTO> tblvGSTR1CreditDebitRegisteredList;
    @FXML
    private TableColumn<GSTR1CreditDebitRegisteredDTO, String> tblGSTR1CreditDebitRegisteredDates, tblGSTR1CreditDebitRegisteredInvoiceNo,
            tblGSTR1CreditDebitRegisteredParticulars, tblGSTR1CreditDebitRegisteredGSTIN, tblGSTR1CreditDebitRegisteredVoucherType,
            tblGSTR1CreditDebitRegisteredTaxableAmt, tblGSTR1CreditDebitRegisteredIGSTAmt, tblGSTR1CreditDebitRegisteredCGSTAmt,
            tblGSTR1CreditDebitRegisteredSGSTAmt, tblGSTR1CreditDebitRegisteredCessAmt,
            tblGSTR1CreditDebitRegisteredTaxAmt, tblGSTR1CreditDebitRegisteredInvoiceAmt;
    @FXML
    private TextField tfGSTR1CreditDebitRegisteredSearch;

    private ObservableList<GSTR1CreditDebitRegisteredDTO> originalData;

    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;

    @FXML
    private BorderPane borderpane;

    @FXML
    private Label lblTotalTaxable_amt, lblTotalIgst_amt, lblTotalCgst_amt, lblTotalSgst_amt, lblTotalTax_amt, lblTotalInvoice_amt;

    String Start_date = "", End_date = "";

    @FXML
    private TextField tfStartDt, tfEndDt;

    private JsonObject jsonObject = null;

    public void initialize(URL url, ResourceBundle resourceBundle) {

        //TODO: resizing the table columns as per the resolution.
        tblvGSTR1CreditDebitRegisteredList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        responsiveTableDesign();


        GSTR1CreditDebitRegisteredListLogger.info("Start of Initialize method of : GSTR1CreditDebitRegisteredController");
        Platform.runLater(() -> tfGSTR1CreditDebitRegisteredSearch.requestFocus());
        fetchGSTR1CreditDebitRegisteredList("");
        borderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR1_DASHBOARD_LIST_SLUG, false);
            }
        });

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                fetchGSTR1CreditDebitRegisteredList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR1CreditDebitRegisteredSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        nodetraversal(tfGSTR1CreditDebitRegisteredSearch, tfStartDt);
        nodetraversal(tfStartDt, tfEndDt);
        nodetraversal(tfEndDt, btExportPdf);
        nodetraversal(btExportPdf, btExportExcel);
        nodetraversal(btExportExcel, btExportCsv);
        nodetraversal(btExportCsv, btExportPrint);
        tblvGSTR1CreditDebitRegisteredList.requestFocus();
    }

    private void nodetraversal(Node current_node, Node next_node) {
        current_node.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if ("tfEndDt".equals(current_node.getId())) {
                    getEndDate();
                }
                next_node.requestFocus();
                event.consume();
            }
            if (event.getCode() == KeyCode.TAB) {
                if ("tfEndDt".equals(current_node.getId())) {
                    getEndDate();
                }
            }

            if (event.getCode() == KeyCode.SPACE) {
                if (current_node instanceof Button button) {
                    button.fire();
                }
            }
        });
    }

    private void getEndDate() {
//        System.out.println("tfEndDt-------> " + tfEndDt.getText());
        fetchGSTR1CreditDebitRegisteredList("");
    }

    private void responsiveTableDesign() {
        tblGSTR1CreditDebitRegisteredDates.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredInvoiceNo.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredParticulars.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.12));
        tblGSTR1CreditDebitRegisteredGSTIN.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredVoucherType.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredTaxableAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredIGSTAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredCGSTAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredSGSTAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredCessAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredTaxAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
        tblGSTR1CreditDebitRegisteredInvoiceAmt.prefWidthProperty().bind(tblvGSTR1CreditDebitRegisteredList.widthProperty().multiply(0.08));
    }

    private void fetchGSTR1CreditDebitRegisteredList(String searchKey) {
        GSTR1CreditDebitRegisteredListLogger.info("Fetch GSTR3B Outward Taxable Supplies List  : GSTR1CreditDebitRegisteredController");

        APIClient apiClient = null;

        try {
            GSTR1CreditDebitRegisteredListLogger.debug("Get GSTR3B Outward Taxable Supplies Data Started...");
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
//            HttpResponse<String> response = APIClient.postFormDataRequest(requestBody, EndPoints.GSTR3B_OutwardTaxableSupplies_LIST);
            apiClient = new APIClient(EndPoints.GSTR1_CREDIT_DEBIT_REGISTERED_9B, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
//                    JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//            System.out.println("fetchGSTR1CreditDebitRegisteredList" + responseBody);
//                    System.out.println("jsonObject--------------------------" + jsonObject);
                    ObservableList<GSTR1CreditDebitRegisteredDTO> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
//                System.out.println("fetchGSTR1CreditDebitRegisteredList" + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);

                        if (responseList.size() > 0) {
                            tblvGSTR1CreditDebitRegisteredList.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id") != null ? item.get("id").getAsString() : "";
                                String dates = item.get("transaction_date") != null ? item.get("transaction_date").getAsString() : "";
                                String invoice_no = item.get("invoice_no") != null ? item.get("invoice_no").getAsString() : "";
                                String particulars = item.get("ledger_name") != null ? item.get("ledger_name").getAsString() : "";
                                String gstin_uin = item.get("gst_number") != null ? item.get("gst_number").getAsString() : "";
                                String voucher_type = item.get("voucher_type") != null ? item.get("voucher_type").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("total_igst") != null ? item.get("total_igst").getAsString() : "";
                                String cgst_amt = item.get("total_cgst") != null ? item.get("total_cgst").getAsString() : "";
                                String sgst_amt = item.get("total_sgst") != null ? item.get("total_sgst").getAsString() : "";
                                String cess_amt = item.get("") != null ? item.get("").getAsString() : "";
                                String tax_amt = item.get("total_tax") != null ? item.get("total_tax").getAsString() : "";
                                String invoice_amt = item.get("total_amt") != null ? item.get("total_amt").getAsString() : "";

                                observableList.add(new GSTR1CreditDebitRegisteredDTO(id, dates, invoice_no, particulars, gstin_uin, voucher_type, taxable_amt, igst_amt, cgst_amt, sgst_amt, cess_amt, tax_amt, invoice_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblGSTR1CreditDebitRegisteredDates.setCellValueFactory(new PropertyValueFactory<>("dates"));
                            tblGSTR1CreditDebitRegisteredInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoice_no"));
                            tblGSTR1CreditDebitRegisteredParticulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblGSTR1CreditDebitRegisteredGSTIN.setCellValueFactory(new PropertyValueFactory<>("gstin_uin"));
                            tblGSTR1CreditDebitRegisteredVoucherType.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                            tblGSTR1CreditDebitRegisteredTaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblGSTR1CreditDebitRegisteredIGSTAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblGSTR1CreditDebitRegisteredCGSTAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblGSTR1CreditDebitRegisteredSGSTAmt.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblGSTR1CreditDebitRegisteredCessAmt.setCellValueFactory(new PropertyValueFactory<>("cess_amt"));
                            tblGSTR1CreditDebitRegisteredTaxAmt.setCellValueFactory(new PropertyValueFactory<>("tax_amt"));
                            tblGSTR1CreditDebitRegisteredInvoiceAmt.setCellValueFactory(new PropertyValueFactory<>("invoice_amt"));

                            tblvGSTR1CreditDebitRegisteredList.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR1CreditDebitRegisteredListLogger.debug("Success in Displaying GSTR3B Outward Taxable Supplies List : GSTR1CreditDebitRegisteredController");

                        } else {
                            GSTR1CreditDebitRegisteredListLogger.debug("GSTR3B Outward Taxable Supplies List ResponseObject is null : GSTR1CreditDebitRegisteredController");
                            tblvGSTR1CreditDebitRegisteredList.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR1CreditDebitRegisteredListLogger.debug("Error in response of GSTR3B Outward Taxable Supplies List : GSTR1CreditDebitRegisteredController");
                        tblvGSTR1CreditDebitRegisteredList.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1CreditDebitRegisteredListLogger.error("Network API cancelled in fetchGSTR1CreditDebitRegisteredList()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1CreditDebitRegisteredListLogger.error("Network API failed in fetchGSTR1CreditDebitRegisteredList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR1CreditDebitRegisteredListLogger.debug("Get GSTR3B Outward Taxable Supplies Data End...");

        } catch (Exception e) {
            GSTR1CreditDebitRegisteredListLogger.error("GSTR3B Outward Taxable Supplies List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }

    }

    private void calculateTotalAmount() {
        ObservableList<GSTR1CreditDebitRegisteredDTO> filteredData = tblvGSTR1CreditDebitRegisteredList.getItems();
        // Calculate the Totals
        double totalTaxable_amt = 0.0;
        double totalIgst_amt = 0.0;
        double totalCgst_amt = 0.0;
        double totalSgst_amt = 0.0;
        double totalTax_amt = 0.0;
        double totalInvoice_amt = 0.0;
        for (GSTR1CreditDebitRegisteredDTO item : filteredData) {
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


    private void filterData(String keyword) {
        ObservableList<GSTR1CreditDebitRegisteredDTO> filteredData = FXCollections.observableArrayList();

        GSTR1CreditDebitRegisteredListLogger.error("Search GSTR3B Outward Taxable Supplies in GSTR1CreditDebitRegisteredController");
        if (keyword.isEmpty()) {
            tblvGSTR1CreditDebitRegisteredList.setItems(originalData);
            return;
        }

        for (GSTR1CreditDebitRegisteredDTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR1CreditDebitRegisteredList.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR1CreditDebitRegisteredDTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getDates().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getInvoice_no().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getGstin_uin().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getCess_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getTax_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getInvoice_amt().toLowerCase().contains(lowerCaseKeyword);
    }
}
