package com.opethic.genivis.controller.Reports;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opethic.genivis.controller.commons.SwitchButton;
import com.opethic.genivis.dto.LedgerReport3DTO;
import com.opethic.genivis.dto.TransactionCreditNote1DTO;
import com.opethic.genivis.dto.reqres.product.Communicator;
import com.opethic.genivis.models.HeadType;
import com.opethic.genivis.models.master.ledger.BalanceType;
import com.opethic.genivis.models.master.ledger.CommonStringOption;
import com.opethic.genivis.models.master.ledger.TaxType;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.opethic.genivis.utils.FxmFileConstants.*;

public class TransactionCreditNote1Controller implements Initializable {

    private static final Logger TransactionCreditNote1Logger = LoggerFactory.getLogger(TransactionCreditNote1Controller.class);
    @FXML
    private TableView<TransactionCreditNote1DTO> tblListCreditNote1;
    @FXML
    private TableColumn<TransactionCreditNote1DTO, String> tblListCreditNote1Date, tblListCreditNote1Particulars, tblListCreditNote1VoucherTy, tblListCreditNote1VoucherNo, tblListCreditNote1DebitAmt, tblListCreditNote1CreditAmt;
    @FXML
    private TextField searchCreditNote1;

    @FXML
    private ComboBox<CommonStringOption> comboboxTypeCreditNote1, comboboxDurationCreditNote1;

    private ObservableList<TransactionCreditNote1DTO> originalData;

    @FXML
    private Label lblTotalDebit;
    @FXML

    private Label lblTotalCredit;
    @FXML
    private TextField fromDateCreditNote1, toDateCreditNote1;

    private boolean isFunded = true;
    //    @FXML
//    private SwitchButton sbWithBalance;
    @FXML
    private TextField fromAmountCreditNote1;
    @FXML
    private TextField toAmountCreditNote1;
    @FXML
    private Button resetBtnCreditNote1,btnPdfCreditNote,btnExcelCreditNote,btnCsvCreditNote,btnPrintCreditNote;
    @FXML
    private boolean filterByDate = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initData();
    }

    private void initData() {

        TransactionCreditNote1Logger.info("Start of Initialize method of : TransactionCreditNote1Controller");

        getFilterTypeList();
        getDurationList();
        fetchCreditNote1List("");

        DateValidator.applyDateFormat(fromDateCreditNote1);
        DateValidator.applyDateFormat(toDateCreditNote1);

        searchCreditNote1.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue.trim());
        });

        fromAmountCreditNote1.textProperty().addListener((obs, oldValue, newValueFromAmt) -> {
            fromAmountCreditNote1.setText(newValueFromAmt.matches("^[0-9]*\\.?[0-9]*$") ? newValueFromAmt : oldValue);
        });

        toAmountCreditNote1.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {

            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                if (!fromAmountCreditNote1.getText().isEmpty() && !toAmountCreditNote1.getText().isEmpty()) {
                    filterCreditListByAmount();
                } else {
//                    fetchCreditNote1List("");
                }

            }
        });

        toAmountCreditNote1.textProperty().addListener((obs, oldValue, newValueToAmt) -> {
//            System.out.println("To Amount: " + newValueToAmt.trim());
            toAmountCreditNote1.setText(newValueToAmt.matches("^[0-9]*\\.?[0-9]*$") ? newValueToAmt : oldValue);
//            toAmountCreditNote1.setText(newValueToAmt.trim());
        });
        toDateCreditNote1.addEventHandler(KeyEvent.ANY, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (fromDateCreditNote1.getText() != null && toDateCreditNote1.getText() != null) {
                    filterByDate = true;
                    fetchCreditNote1List("");
                } else {
                    filterByDate = false;
                    fetchCreditNote1List("");
                }
            }
        });

        setKeyPressedHandler(searchCreditNote1);
        setKeyPressedHandler(comboboxDurationCreditNote1);
        setKeyPressedHandler(fromDateCreditNote1);
        setKeyPressedHandler(toDateCreditNote1);
        setKeyPressedHandler(fromAmountCreditNote1);
        setKeyPressedHandler(toAmountCreditNote1);
        setKeyPressedHandler(resetBtnCreditNote1);
        setKeyPressedHandler(btnPdfCreditNote);
        setKeyPressedHandler(btnExcelCreditNote);
        setKeyPressedHandler(btnCsvCreditNote);
        setKeyPressedHandler(btnCsvCreditNote);

        Platform.runLater(() -> {
            searchCreditNote1.requestFocus();
        });
        comboboxDurationCreditNote1.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.SPACE){
                comboboxDurationCreditNote1.show();
            }
        });

    }

    private void getFilterTypeList() {
        List<CommonStringOption> lst = Globals.getFilterType();
        ObservableList<CommonStringOption> lstFilterType = FXCollections.observableArrayList(lst);
        comboboxTypeCreditNote1.getItems().clear();
        comboboxTypeCreditNote1.getItems().addAll(lstFilterType);
        comboboxTypeCreditNote1.setConverter(new StringConverter<CommonStringOption>() {
            @Override
            public String toString(CommonStringOption taxType) {
                return taxType != null ? taxType.getValue() : "";
            }

            @Override
            public CommonStringOption fromString(String string) {
                // You can implement this method if needed
                return null;
            }
        });
        comboboxTypeCreditNote1.setValue(lstFilterType.get(0));


    }

    private void getDurationList() {
        List<CommonStringOption> lst = Globals.getDurationType();
        ObservableList<CommonStringOption> lstFilterType = FXCollections.observableArrayList(lst);

        comboboxDurationCreditNote1.getItems().clear();
        comboboxDurationCreditNote1.getItems().addAll(lstFilterType);
        comboboxDurationCreditNote1.setConverter(new StringConverter<CommonStringOption>() {
            @Override
            public String toString(CommonStringOption taxType) {
                return taxType != null ? taxType.getLabel() : "";
            }

            @Override
            public CommonStringOption fromString(String string) {
                // You can implement this method if needed
                return null;
            }
        });
        comboboxDurationCreditNote1.setValue(lstFilterType.get(0));
        comboboxDurationCreditNote1.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if(comboboxDurationCreditNote1.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("fullYear")){
                GlobalController.getInstance().addTabStatic(TRANSACTION_CREDIT_NOTE2_LIST_SLUG, false);
            }else{
                fetchCreditNote1List("");
            }

        });
    }

    private void fetchCreditNote1List(String searchKey) {
        TransactionCreditNote1Logger.info("fetch Transaction Credit Note 1 List : TransactionCreditNote1Controller");
        try {
            Map<String, String> body = new HashMap<>();

            if (filterByDate) {
                body.put("start_date", Communicator.text_to_date.fromString(fromDateCreditNote1.getText()).toString());
                body.put("end_date", Communicator.text_to_date.fromString(toDateCreditNote1.getText()).toString());
            } else {
                body.put("duration", comboboxDurationCreditNote1.getSelectionModel().getSelectedItem().getValue());

            }

            String formData = Globals.mapToStringforFormData(body);
            HttpResponse<String> response = APIClient.postFormDataRequest(formData, EndPoints.TRANSACTION_CREDIT_NOTE1_LIST);
            JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
//            System.out.println("LedgerReport1_list" + responseBody);
            ObservableList<TransactionCreditNote1DTO> observableList = FXCollections.observableArrayList();
            if (responseBody.get("responseStatus").getAsInt() == 200) {
                JsonArray responseList = responseBody.getAsJsonArray("response");

                String startDate = responseBody.get("d_start_date").getAsString();
                LocalDate start_date = LocalDate.parse(startDate);
                String endDate = responseBody.get("d_end_date").getAsString();
                LocalDate  end_date = LocalDate.parse(endDate);
                fromDateCreditNote1.setText(start_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                toDateCreditNote1.setText(end_date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                fromDateCreditNote1.setText(!responseBody.get("d_start_date").getAsString().isEmpty() ? DateConvertUtil.convertStringToLocalDate(responseBody.get("d_start_date").getAsString()) : null);

//                toDateCreditNote1.setText(!responseBody.get("d_end_date").getAsString().isEmpty() ? DateConvertUtil.convertStringToLocalDate(responseBody.get("d_end_date").getAsString()) : null);

                if (responseList.size() > 0) {

                    filterByDate = false;
                    tblListCreditNote1.setVisible(true);
                    for (JsonElement element : responseList) {
                        JsonObject item = element.getAsJsonObject();
                        String id = item.get("voucher_id").getAsString();
                        String transaction_date = item.get("transaction_date").getAsString();
                        String particulars = item.get("particulars").getAsString();
                        String type = item.get("voucher_type") != null ? item.get("voucher_type").getAsString() : "";
                        String voucher_no = item.get("voucher_no").getAsString();
                        String debit = item.get("debit").getAsString();
                        String credit = item.get("credit").getAsString();

                        observableList.add(new TransactionCreditNote1DTO(id, transaction_date, particulars, type, voucher_no, debit, credit));
                    }


                    tblListCreditNote1Date.setCellValueFactory(new PropertyValueFactory<>("date"));
                    tblListCreditNote1Particulars.setCellValueFactory(new PropertyValueFactory<>("particulars"));
                    tblListCreditNote1VoucherTy.setCellValueFactory(new PropertyValueFactory<>("voucher_type"));
                    tblListCreditNote1VoucherNo.setCellValueFactory(new PropertyValueFactory<>("voucher_no"));
                    tblListCreditNote1DebitAmt.setCellValueFactory(new PropertyValueFactory<>("debit_amt"));
                    tblListCreditNote1CreditAmt.setCellValueFactory(new PropertyValueFactory<>("credit_amt"));

                    tblListCreditNote1.setItems(observableList);
                    originalData = observableList;

                    calculationTotal();
//                    Platform.runLater(() -> {
//                        searchCreditNote1.requestFocus();
//
//                    });
                    TransactionCreditNote1Logger.debug("Success in Displaying Transaction Credit Note 1 List : TransactionCreditNote1Controller");
                } else {
                    tblListCreditNote1.getItems().clear();
                    calculationTotal();
                    TransactionCreditNote1Logger.debug("Transaction Credit Note 1 List ResponseObject is null : TransactionCreditNote1Controller");
                }
            } else {
                tblListCreditNote1.getItems().clear();
                calculationTotal();
                TransactionCreditNote1Logger.debug("Error in response of Transaction Credit Note 1 List : TransactionCreditNote1Controller");
            }
        } catch (Exception e) {
            TransactionCreditNote1Logger.error("Transaction Credit Note 1 Error is " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterData(String keyword) {
        ObservableList<TransactionCreditNote1DTO> filteredData = FXCollections.observableArrayList();
        TransactionCreditNote1Logger.error("Search Transaction Credit Note 1 in TransactionCreditNote1Controller");
        if (keyword.isEmpty()) {
            tblListCreditNote1.setItems(originalData);
            calculationTotal();
            return;
        }

        for (TransactionCreditNote1DTO item : originalData) {
            if (matchesKeyword(item, keyword)) {
                filteredData.add(item);
            }
        }

        tblListCreditNote1.setItems(filteredData);
        calculationTotal();
    }

    private boolean matchesKeyword(TransactionCreditNote1DTO item, String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();

        return item.getDate().toLowerCase().contains(lowerCaseKeyword) ||
                item.getParticulars_name().toLowerCase().contains(lowerCaseKeyword) ||
                item.getVoucher_type().toLowerCase().contains(lowerCaseKeyword) ||
                item.getVoucher_no().toLowerCase().contains(lowerCaseKeyword) ||
                item.getDebit_amt().toLowerCase().contains(lowerCaseKeyword) ||
                item.getCredit_amt().toLowerCase().contains(lowerCaseKeyword);
    }

    @FXML
    private void handleResetAction() {
        fromAmountCreditNote1.setText("");
        toAmountCreditNote1.setText("");
        initData();
//        getFilterTypeList();
//        getDurationList();
//        fetchCreditNote1List("");
    }

    private void calculationTotal() {
        ObservableList<TransactionCreditNote1DTO> filteredData = tblListCreditNote1.getItems();
        // Calculate the Totals
        double totalDebit = 0.0;
        double totalCredit = 0.0;
        for (TransactionCreditNote1DTO item : filteredData) {
            totalDebit += Double.parseDouble(item.getDebit_amt().isEmpty() ? "0.0" : item.getDebit_amt());
            totalCredit += Double.parseDouble(item.getCredit_amt().isEmpty() ? "0.0" : item.getCredit_amt());
        }

        // Update Labels in the FXML
        lblTotalDebit.setText(String.format("%.2f", totalDebit));
        lblTotalCredit.setText(String.format("%.2f", totalCredit));
    }

    private void filterCreditListByAmount() {
        ObservableList<TransactionCreditNote1DTO> filteredData = FXCollections.observableArrayList();

        TransactionCreditNote1Logger.error("Search Transaction Credit Note 1 in TransactionCreditNote1Controller");
        Double tfFromAmount = Double.parseDouble(fromAmountCreditNote1.getText());
        Double tfToAmount = Double.parseDouble(toAmountCreditNote1.getText());
        filteredData = originalData.stream().filter((item -> ((Double.parseDouble(item.getDebit_amt()) > 0 && Double.parseDouble(item.getDebit_amt()) >= tfFromAmount && Double.parseDouble(item.getDebit_amt()) <= tfToAmount) || (Double.parseDouble(item.getCredit_amt()) > 0 && Double.parseDouble(item.getCredit_amt()) >= tfFromAmount && Double.parseDouble(item.getCredit_amt()) <= tfToAmount)))).collect(Collectors.toCollection(FXCollections::observableArrayList));

        tblListCreditNote1.setItems(filteredData);
        calculationTotal();
    }
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
}
