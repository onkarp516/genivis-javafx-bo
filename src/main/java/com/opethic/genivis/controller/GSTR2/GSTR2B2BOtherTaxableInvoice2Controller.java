package com.opethic.genivis.controller.GSTR2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.dto.GSTR2.GSTR2B2BOtherTaxableInvoice2DTO;
import com.opethic.genivis.dto.GSTR2.GSTR2B2BOtherTaxableInvoiceDTO;
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

import static com.opethic.genivis.utils.FxmFileConstants.GSTR2_B2B_Other_Taxable_Invoice_LIST_SLUG;
import static com.opethic.genivis.utils.FxmFileConstants.GSTR2_DASHBOARD_LIST_SLUG;

public class GSTR2B2BOtherTaxableInvoice2Controller implements Initializable {
    private static final Logger GSTR2B2BOtherTaxableInvoice2ListLogger = LoggerFactory.getLogger(GSTR2B2BOtherTaxableInvoice2Controller.class);
    @FXML
    private TableView<GSTR2B2BOtherTaxableInvoice2DTO> tblvGSTR2B2BOtherTaxableInvoice2List;
    @FXML
    private TableColumn<GSTR2B2BOtherTaxableInvoice2DTO, String> tblGSTR2B2BOtherTaxableInvoice2SrNo, tblGSTR2B2BOtherTaxableInvoice2Particulars,
            tblGSTR2B2BOtherTaxableInvoice2Dates, tblGSTR2B2BOtherTaxableInvoice2InvoiceNo, tblGSTR2B2BOtherTaxableInvoice2TaxableAmt,
            tblGSTR2B2BOtherTaxableInvoice2IGSTAmt, tblGSTR2B2BOtherTaxableInvoice2CGSTAmt, tblGSTR2B2BOtherTaxableInvoice2SGSTAmt,
            tblGSTR2B2BOtherTaxableInvoice2CessAmt, tblGSTR2B2BOtherTaxableInvoice2TaxAmt, tblGSTR2B2BOtherTaxableInvoice2InvoiceAmt;
    private ObservableList<GSTR2B2BOtherTaxableInvoice2DTO> originalData;
    @FXML
    private BorderPane mainBorderpane;
    @FXML
    private TextField tfGSTR2B2BOtherTaxableInvoice2Search, tfStartDt, tfEndDt;
    @FXML
    private Button btExportPdf, btExportExcel, btExportCsv, btExportPrint;
    @FXML
    private Label lblTotalTaxable_amt, lblTotalIgst_amt, lblTotalCgst_amt, lblTotalSgst_amt, lblTotalTax_amt, lblTotalInvoice_amt;

    private JsonObject jsonObject = null;

    String Start_date = "", End_date = "";

    public static String GSTR2B2BOtherTaxableInvoiceId = "";
    public static String GSTR2B2BOtherTaxableInvoiceStartDate = "";
    public static String GSTR2B2BOtherTaxableInvoiceEndDate = "";

    public void initialize(URL url, ResourceBundle resourceBundle) {
        //TODO: resizing the table columns as per the resolution.
        tblvGSTR2B2BOtherTaxableInvoice2List.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
//        System.out.println("GSTR2B2BOtherTaxableInvoiceId-----> " + GSTR2B2BOtherTaxableInvoiceId);
//        System.out.println("GSTR2B2BOtherTaxableInvoiceStartDate-----> " + GSTR2B2BOtherTaxableInvoiceStartDate);
//        System.out.println("GSTR2B2BOtherTaxableInvoiceEndDate-----> " + GSTR2B2BOtherTaxableInvoiceEndDate);
        responsiveTableDesign();

        Platform.runLater(() -> tfGSTR2B2BOtherTaxableInvoice2Search.requestFocus());

        mainBorderpane.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
//                System.out.println("Esc Clicked....!");
                GlobalController.getInstance().addTabStatic(GSTR2_B2B_Other_Taxable_Invoice_LIST_SLUG, false);
            }
        });

        Start_date = GSTR2B2BOtherTaxableInvoiceStartDate;
        End_date = GSTR2B2BOtherTaxableInvoiceEndDate;
        LocalDate startLocalDt = LocalDate.parse(Start_date);
        LocalDate endLocalDt = LocalDate.parse(End_date);

        tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        GSTR2B2BOtherTaxableInvoice2List("");

        tfEndDt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("DateFilter From Date " + tfStartDt.getText().toString());
//                System.out.println("DateFilter To Date " + tfEndDt.getText().toString());
                GSTR2B2BOtherTaxableInvoice2List("");
            }
        });

        DateValidator.applyDateFormat(tfStartDt);
        DateValidator.applyDateFormat(tfEndDt);

        tfGSTR2B2BOtherTaxableInvoice2Search.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        // Setting the handler for each node
        setKeyPressedHandler(tfGSTR2B2BOtherTaxableInvoice2Search);
        setKeyPressedHandler(tfStartDt);
        setKeyPressedHandler(tfEndDt);
        setKeyPressedHandler(btExportPdf);
        setKeyPressedHandler(btExportExcel);
        setKeyPressedHandler(btExportCsv);
        setKeyPressedHandler(btExportPrint);
        tblvGSTR2B2BOtherTaxableInvoice2List.requestFocus();

        tfEndDt.setOnKeyPressed(event ->{
            if(event.getCode() == KeyCode.ENTER){
                GSTR2B2BOtherTaxableInvoice2List("");
                Node nextNode = CommonTraversa.getNextFocusableNode(btExportPdf.getScene());
                if (nextNode != null) {
                    nextNode.requestFocus();
                }
            }
            if(event.getCode() == KeyCode.TAB){
                GSTR2B2BOtherTaxableInvoice2List("");
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
        tblGSTR2B2BOtherTaxableInvoice2SrNo.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.05));
        tblGSTR2B2BOtherTaxableInvoice2Particulars.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.23));
        tblGSTR2B2BOtherTaxableInvoice2Dates.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2InvoiceNo.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2TaxableAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2IGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2CGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2SGSTAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2CessAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2TaxAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
        tblGSTR2B2BOtherTaxableInvoice2InvoiceAmt.prefWidthProperty().bind(tblvGSTR2B2BOtherTaxableInvoice2List.widthProperty().multiply(0.08));
    }

    private void GSTR2B2BOtherTaxableInvoice2List(String searchKey) {
        GSTR2B2BOtherTaxableInvoice2ListLogger.info("Fetch GSTR2 B2B Other Taxable Invoice 2 List  : GSTR2B2BOtherTaxableInvoice2Controller");
        APIClient apiClient = null;
        try {
            GSTR2B2BOtherTaxableInvoice2ListLogger.debug("Get GSTR2 B2B Other Taxable Invoice 2 Data Started...");
            Map<String, String> body = new HashMap<>();
            body.put("creditor_id", GSTR2B2BOtherTaxableInvoiceId);
            body.put("searchText", "");
            body.put("startDate", Communicator.text_to_date.fromString(tfStartDt.getText()).toString());
            body.put("endDate", Communicator.text_to_date.fromString(tfEndDt.getText()).toString());
            System.out.println("body ----------->  " + body);
            String requestBody = Globals.mapToString(body);
            apiClient = new APIClient(EndPoints.GSTR2_B2B_Other_Taxable_2, requestBody, RequestType.JSON);
            apiClient.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    jsonObject = new Gson().fromJson(workerStateEvent.getSource().getValue().toString(), JsonObject.class);
                    ObservableList<GSTR2B2BOtherTaxableInvoice2DTO> observableList = FXCollections.observableArrayList();
                    System.out.println("jsonObject12345 " + jsonObject);
                    if (jsonObject.get("responseStatus").getAsInt() == 200) {
                        JsonArray responseList = jsonObject.getAsJsonArray("responseObject");
                        System.out.println("GSTR2CreditDebitRegisterList---> " + responseList);
                        Start_date = GSTR2B2BOtherTaxableInvoiceStartDate;
                        End_date = GSTR2B2BOtherTaxableInvoiceEndDate;
                        LocalDate startLocalDt = LocalDate.parse(Start_date);
                        LocalDate endLocalDt = LocalDate.parse(End_date);
//                        System.out.println(startLocalDt + " <-start end-> " + endLocalDt);
                        int countSr = 1;
                        if (responseList.size() > 0) {
                            tblvGSTR2B2BOtherTaxableInvoice2List.setVisible(true);
                            for (JsonElement element : responseList) {
                                JsonObject item = element.getAsJsonObject();
                                String id = item.get("id") != null ? item.get("id").getAsString() : "";
                                String particulars = item.get("sundry_creditor_name") != null ? item.get("sundry_creditor_name").getAsString() : "";
                                String dates = item.get("invoice_date") != null ? item.get("invoice_date").getAsString() : "";
                                String invoice_no = item.get("invoice_no") != null ? item.get("invoice_no").getAsString() : "";
                                String taxable_amt = item.get("taxable_amt") != null ? item.get("taxable_amt").getAsString() : "";
                                String igst_amt = item.get("igst") != null ? item.get("igst").getAsString() : "";
                                String cgst_amt = item.get("cgst") != null ? item.get("cgst").getAsString() : "";
                                String sgst_amt = item.get("sgst") != null ? item.get("sgst").getAsString() : "";
                                String cess_amt = item.get("") != null ? item.get("").getAsString() : "";
                                String tax_amt = item.get("tax_amt") != null ? item.get("tax_amt").getAsString() : "";
                                String invoice_amt = item.get("total_amount") != null ? item.get("total_amount").getAsString() : "";

                                observableList.add(new GSTR2B2BOtherTaxableInvoice2DTO(String.valueOf(countSr++), id, particulars, dates, invoice_no, taxable_amt, igst_amt, cgst_amt, sgst_amt, cess_amt, tax_amt, invoice_amt));
                            }

                            tfStartDt.setText(startLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            tfEndDt.setText(endLocalDt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                            tblGSTR2B2BOtherTaxableInvoice2SrNo.setCellValueFactory(new PropertyValueFactory<>("sr_no"));
                            tblGSTR2B2BOtherTaxableInvoice2Particulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                            tblGSTR2B2BOtherTaxableInvoice2Dates.setCellValueFactory(new PropertyValueFactory<>("dates"));
                            tblGSTR2B2BOtherTaxableInvoice2InvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoice_no"));
                            tblGSTR2B2BOtherTaxableInvoice2TaxableAmt.setCellValueFactory(new PropertyValueFactory<>("taxable_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2IGSTAmt.setCellValueFactory(new PropertyValueFactory<>("igst_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2CGSTAmt.setCellValueFactory(new PropertyValueFactory<>("cgst_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2SGSTAmt.setCellValueFactory(new PropertyValueFactory<>("sgst_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2CessAmt.setCellValueFactory(new PropertyValueFactory<>("cess_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2TaxAmt.setCellValueFactory(new PropertyValueFactory<>("tax_amt"));
                            tblGSTR2B2BOtherTaxableInvoice2InvoiceAmt.setCellValueFactory(new PropertyValueFactory<>("invoice_amt"));

                            tblvGSTR2B2BOtherTaxableInvoice2List.setItems(observableList);

                            originalData = observableList;
                            calculateTotalAmount();
                            GSTR2B2BOtherTaxableInvoice2ListLogger.debug("Success in Displaying GSTR2 B2B Other Taxable Invoice 2 List : GSTR2B2BOtherTaxableInvoice2Controller");


                        } else {
                            GSTR2B2BOtherTaxableInvoice2ListLogger.debug("GSTR2 B2B Other Taxable Invoice 2 List ResponseObject is null : GSTR2B2BOtherTaxableInvoice2Controller");
                            tblvGSTR2B2BOtherTaxableInvoice2List.getItems().clear();
                            calculateTotalAmount();
                        }
                    } else {
                        GSTR2B2BOtherTaxableInvoice2ListLogger.debug("Error in response of GSTR2 B2B Other Taxable Invoice 2 List : GSTR2B2BOtherTaxableInvoice2Controller");
                        tblvGSTR2B2BOtherTaxableInvoice2List.getItems().clear();
                        calculateTotalAmount();
                    }
                }
            });

            apiClient.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2B2BOtherTaxableInvoice2ListLogger.error("Network API cancelled in GSTR2B2BOtherTaxableInvoice2List()" + workerStateEvent.getSource().getValue().toString());
                }
            });

            apiClient.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    GSTR2B2BOtherTaxableInvoice2ListLogger.error("Network API failed in GSTR2B2BOtherTaxableInvoice2List()" + workerStateEvent.getSource().getValue().toString());
                }
            });
            apiClient.start();
            GSTR2B2BOtherTaxableInvoice2ListLogger.debug("Get GSTR2 B2B Other Taxable Invoice 2 Data End...");

        } catch (Exception e) {
            GSTR2B2BOtherTaxableInvoice2ListLogger.error("GSTR2 B2B Other Taxable Invoice 2 List Error is " + e.getMessage());
            e.printStackTrace();
        } finally {
            apiClient = null;
        }
    }

    private void filterData(String keyword) {
        ObservableList<GSTR2B2BOtherTaxableInvoice2DTO> filteredData = FXCollections.observableArrayList();

        GSTR2B2BOtherTaxableInvoice2ListLogger.error("Search GSTR2 B2B Other Taxable Invoice in GSTR2B2BOtherTaxableInvoiceController");
        if (keyword.isEmpty()) {
            tblvGSTR2B2BOtherTaxableInvoice2List.setItems(originalData);
            return;
        }

        for (GSTR2B2BOtherTaxableInvoice2DTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblvGSTR2B2BOtherTaxableInvoice2List.setItems(filteredData);
        calculateTotalAmount();
    }

    private boolean matchesKeyword(GSTR2B2BOtherTaxableInvoice2DTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        return
                item.getParticulars().toLowerCase().contains(lowerCaseKeyword) ||
                item.getDates().toLowerCase().contains(lowerCaseKeyword) ||
                item.getInvoice_no().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTaxable_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getIgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getSgst_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCess_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getTax_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getInvoice_amt().toLowerCase().contains(lowerCaseKeyword);
    }

    private void calculateTotalAmount() {
        ObservableList<GSTR2B2BOtherTaxableInvoice2DTO> filteredData = tblvGSTR2B2BOtherTaxableInvoice2List.getItems();
        // Calculate the Totals
        double totalTaxable_amt = 0.0;
        double totalIgst_amt = 0.0;
        double totalCgst_amt = 0.0;
        double totalSgst_amt = 0.0;
        double totalTax_amt = 0.0;
        double totalInvoice_amt = 0.0;

        for (GSTR2B2BOtherTaxableInvoice2DTO item : filteredData) {
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
