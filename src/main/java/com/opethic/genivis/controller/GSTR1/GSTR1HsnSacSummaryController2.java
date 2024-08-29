package com.opethic.genivis.controller.GSTR1;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR1.GSTR1HsnSacSummaryDTO2;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.network.RequestType;
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

import static com.opethic.genivis.utils.FxmFileConstants.*;

public class GSTR1HsnSacSummaryController2 implements Initializable {
    public static String prv_tax_rate = "";
    public static Integer hsnReportId = -1;

    private static final Logger GSTR1HsnSacSummaryList2Logger = LoggerFactory.getLogger(GSTR1HsnSacSummaryController2.class);
    @FXML
    private TableView<GSTR1HsnSacSummaryDTO2> tblvGSTR1HsnSacSummary2List;
    @FXML
    private TableColumn<GSTR1HsnSacSummaryDTO2, String> tblGSTR1HsnSacSummary2SrNo, tblGSTR1HsnSacSummary2Dates,
            tblGSTR1HsnSacSummary2Invoice_no, tblGSTR1HsnSacSummary2Particulars, tblGSTR1HsnSacSummary2Gstin_no,
            tblGSTR1HsnSacSummary2Voucher_type, tblGSTR1HsnSacSummary2Unit_name, tblGSTR1HsnSacSummary2TaxableAmt,
            tblGSTR1HsnSacSummary2IntegratedTaxAmt, tblGSTR1HsnSacSummary2CentralTaxAmt,
            tblGSTR1HsnSacSummary2StateTaxAmount, tblGSTR1HsnSacSummary2TotalTaxAmt;
    @FXML
    private TextField tfGSTR1HsnSacSummary2Search;

    private ObservableList<GSTR1HsnSacSummaryDTO2> originalData;

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
//        System.out.println("--- id ---------- hsn ------> " + hsnReportId);
//        System.out.println("--- prv_tax_rate ---------- hsn ------> " + prv_tax_rate);

        //TODO: resizing the table columns as per the resolution.
        tblvGSTR1HsnSacSummary2List.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        responsiveTableDesign();

        GSTR1HsnSacSummaryList2Logger.info("Start of Initialize method of : GSTR1HsnSacSummaryController2");
        Platform.runLater(() -> tfGSTR1HsnSacSummary2Search.requestFocus());
        fetchGSTR1HsnSacSummaryList("");
        borderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR1_HSN_SAC_SUMMARY_LIST_SLUG, false);
            }
        });

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                fetchGSTR1HsnSacSummaryList("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR1HsnSacSummary2Search.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        nodetraversal(tfGSTR1HsnSacSummary2Search, tfStartDt);
        nodetraversal(tfStartDt, tfEndDt);
        nodetraversal(tfEndDt, btExportPdf);
        nodetraversal(btExportPdf, btExportExcel);
        nodetraversal(btExportExcel, btExportCsv);
        nodetraversal(btExportCsv, btExportPrint);
        tblvGSTR1HsnSacSummary2List.requestFocus();

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
        fetchGSTR1HsnSacSummaryList("");
    }

    private void responsiveTableDesign() {
        tblGSTR1HsnSacSummary2SrNo.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2Dates.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2Invoice_no.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.12));
        tblGSTR1HsnSacSummary2Particulars.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2Gstin_no.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2Voucher_type.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2Unit_name.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2TaxableAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2IntegratedTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2CentralTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2StateTaxAmount.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
        tblGSTR1HsnSacSummary2TotalTaxAmt.prefWidthProperty().bind(tblvGSTR1HsnSacSummary2List.widthProperty().multiply(0.08));
    }

    private void calculateTotalAmount() {
        ObservableList<GSTR1HsnSacSummaryDTO2> filteredData = tblvGSTR1HsnSacSummary2List.getItems();
        // Calculate the Totals
        double taxable_amt = 0.0;
        double igst_amt = 0.0;
        double cgst_amt = 0.0;
        double sgst_amt = 0.0;
        double total_tax_amt = 0.0;
        for (GSTR1HsnSacSummaryDTO2 item : filteredData) {
            taxable_amt += Double.parseDouble(item.getTaxable_amt().isEmpty() ? "0.0" : item.getTaxable_amt());
            igst_amt += Double.parseDouble(item.getIgst_amt().isEmpty() ? "0.0" : item.getIgst_amt());
            cgst_amt += Double.parseDouble(item.getCgst_amt().isEmpty() ? "0.0" : item.getCgst_amt());
            sgst_amt += Double.parseDouble(item.getSgst_amt().isEmpty() ? "0.0" : item.getSgst_amt());
            total_tax_amt += Double.parseDouble(item.getTotal_tax_amt().isEmpty() ? "0.0" : item.getTotal_tax_amt());
        }

        // Update Labels in the FXML
        lblTotalIgst_amt.setText(String.format("%.2f", taxable_amt));
        lblTotalCgst_amt.setText(String.format("%.2f", igst_amt));
        lblTotalSgst_amt.setText(String.format("%.2f", cgst_amt));
        lblTotalTax_amt.setText(String.format("%.2f", sgst_amt));
        lblTotalInvoice_amt.setText(String.format("%.2f", total_tax_amt));
    }

    private void fetchGSTR1HsnSacSummaryList(String searchKey) {
        GSTR1HsnSacSummaryList2Logger.info("Fetch GSTR1 Hsn Sac Summary List  : GSTR1HsnSacSummaryController2");

        APIClient apiClient = null;

        try {
            GSTR1HsnSacSummaryList2Logger.debug("Get GSTR1 Hsn Sac Summary Data Started...");
            Map<String, String> body = new HashMap<>();
            body.put("hsn_id", String.valueOf(hsnReportId));
            body.put("tax_rate", prv_tax_rate);
            if (!tfStartDt.getText().equalsIgnoreCase("") || !tfEndDt.getText().equalsIgnoreCase("")) {
                body.put("start_date", Communicator.text_to_date.fromString(tfStartDt.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(tfEndDt.getText()).toString());
            }
            System.out.println("body ----------->  " + body);
            String requestBody = Globals.mapToStringforFormData(body);
//            HttpResponse<String> response = APIClient.postFormDataRequest(requestBody, EndPoints.GSTR3B_OutwardTaxableSupplies_LIST);
            apiClient = new APIClient(EndPoints.GET_GSTR1_HSN_SAC_SUMMARY2, requestBody, RequestType.FORM_DATA);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
//                    JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
//            System.out.println("fetchGSTR1HsnSacSummaryList---> " + jsonObject);
//                    System.out.println("jsonObject--------------------------" + jsonObject);
                    ObservableList<GSTR1HsnSacSummaryDTO2> observableList = FXCollections.observableArrayList();
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("data");
//                System.out.println("fetchGSTR1HsnSacSummaryList" + responseList);
                        Start_date = (jsonObject.get("start_date").getAsString());
                        End_date = jsonObject.get("end_date").getAsString();
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);
                        int countSr = 1;
                        if (responseList.size() > 0) {
                            tblvGSTR1HsnSacSummary2List.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id") != null ? item.get("id").getAsString() : "";
                                String dates = item.get("invoice_date") != null ? item.get("invoice_date").getAsString() : "";
                                String invoice_no = item.get("invoice_no") != null ? item.get("invoice_no").getAsString() : "";
                                String particulars = item.get("ledger_name") != null ? item.get("ledger_name").getAsString() : "";
                                String gstin_no = item.get("gst_no") != null ? item.get("gst_no").getAsString() : "";
                                String voucher_type = item.get("voucher_type") != null ? item.get("voucher_type").getAsString() : "";
                                String unit_name = item.get("unit_name") != null ? item.get("unit_name").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("total_igst") != null ? item.get("total_igst").getAsString() : "";
                                String cgst_amt = item.get("total_cgst") != null ? item.get("total_cgst").getAsString() : "";
                                String sgst_amt = item.get("total_sgst") != null ? item.get("total_sgst").getAsString() : "";
                                String total_tax_amt = item.get("total_tax") != null ? item.get("total_tax").getAsString() : "";

                                observableList.add(new GSTR1HsnSacSummaryDTO2(String.valueOf(countSr++), id, dates, invoice_no, particulars, gstin_no, voucher_type, unit_name, taxable_amt, igst_amt, cgst_amt, sgst_amt, total_tax_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblGSTR1HsnSacSummary2SrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tblGSTR1HsnSacSummary2Dates.setCellValueFactory(new PropertyValueFactory<>("dates"));
                            tblGSTR1HsnSacSummary2Invoice_no.setCellValueFactory(new PropertyValueFactory<>("invoice_no"));
                            tblGSTR1HsnSacSummary2Particulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblGSTR1HsnSacSummary2Gstin_no.setCellValueFactory(new PropertyValueFactory<>("gstin_no"));
                            tblGSTR1HsnSacSummary2Voucher_type.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                            tblGSTR1HsnSacSummary2Unit_name.setCellValueFactory(new PropertyValueFactory<>("unit_name"));
                            tblGSTR1HsnSacSummary2TaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblGSTR1HsnSacSummary2IntegratedTaxAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblGSTR1HsnSacSummary2CentralTaxAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblGSTR1HsnSacSummary2StateTaxAmount.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblGSTR1HsnSacSummary2TotalTaxAmt.setCellValueFactory(new PropertyValueFactory<>("total_tax_amt"));

                            tblvGSTR1HsnSacSummary2List.setItems(observableList);
                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR1HsnSacSummaryList2Logger.debug("Success in Displaying GSTR1 Hsn Sac Summary List : GSTR1HsnSacSummaryController2");

                        } else {
                            GSTR1HsnSacSummaryList2Logger.debug("GSTR1 Hsn Sac Summary List ResponseObject is null : GSTR1HsnSacSummaryController2");
                            tblvGSTR1HsnSacSummary2List.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR1HsnSacSummaryList2Logger.debug("Error in response of GSTR1 Hsn Sac Summary List : GSTR1HsnSacSummaryController2");
                        tblvGSTR1HsnSacSummary2List.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });
            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1HsnSacSummaryList2Logger.error("Network API cancelled in fetchGSTR1HsnSacSummaryList()" + workerStateEvent.getSource().getValue().toString());

                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR1HsnSacSummaryList2Logger.error("Network API failed in fetchGSTR1HsnSacSummaryList()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR1HsnSacSummaryList2Logger.debug("Get GSTR1 Hsn Sac Summary Data End...");

        } catch (Exception e) {
            GSTR1HsnSacSummaryList2Logger.error("GSTR1 Hsn Sac Summary List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    private void filterData(String keyword) {
        ObservableList<GSTR1HsnSacSummaryDTO2> filteredData = FXCollections.observableArrayList();

        GSTR1HsnSacSummaryList2Logger.error("Search GSTR3B Outward Taxable Supplies in GSTR1CreditDebitRegisteredController");
        if (keyword.isEmpty()) {
            tblvGSTR1HsnSacSummary2List.setItems(originalData);
            return;
        }

        for (GSTR1HsnSacSummaryDTO2 item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR1HsnSacSummary2List.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR1HsnSacSummaryDTO2 item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getDates().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getInvoice_no().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getGstin_no().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getUnit_name().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getTotal_tax_amt().toLowerCase().contains(lowerCaseKeyword);
    }




}
