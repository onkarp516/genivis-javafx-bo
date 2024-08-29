package com.opethic.genivis.controller.tranx_sales.rtn;

import com.google.gson.Gson;
import com.opethic.genivis.GenivisApplication;
import com.opethic.genivis.dto.reqres.sales_tranx.TranxSalesInvoiceListResDTO;
import com.opethic.genivis.dto.reqres.sales_tranx.TranxSalesInvoiceRtnListResDTO;
import com.opethic.genivis.models.tranx.sales.TranxSalesInvoiceListMDL;
import com.opethic.genivis.network.APIClient;
import com.opethic.genivis.network.EndPoints;
import com.opethic.genivis.utils.DateConvertUtil;
import com.opethic.genivis.utils.GlobalController;
import com.opethic.genivis.utils.Globals;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static com.opethic.genivis.utils.FxmFileConstants.*;

public class TranxSalesInvoiceRtnList implements Initializable {
    @FXML
    private BorderPane bpSalesRtnRootPane;

    @FXML
    private TableColumn<TranxSalesInvoiceListMDL, Void> tcAction;

    @FXML
    private TableColumn<?, ?> tcBillAmt;

    @FXML
    private TableColumn<?, ?> tcClientName;

    @FXML
    private TableColumn<?, ?> tcInvoiceNo;

    @FXML
    private TableColumn<?, ?> tcNarration;

    @FXML
    private TableColumn<?, ?> tcPayMode;

    @FXML
    private TableColumn<?, ?> tcPayStatus;

    @FXML
    private TableColumn<?, ?> tcPrint;

    @FXML
    private TableColumn<?, ?> tcSIDate;

    @FXML
    private TableColumn<?, ?> tcTax;

    @FXML
    private TableColumn<?, ?> tcTaxable;

    @FXML
    private TableColumn<?, ?> tcTranxId;

    @FXML
    private TextField tfSalesRtnSearch;

    @FXML
    private TableView<TranxSalesInvoiceListMDL> tvSalesInvoiceList;
    ObservableList<TranxSalesInvoiceListMDL> lstSalesInvoice = FXCollections.observableArrayList();

    public void onBtnCreate(ActionEvent actionEvent) {
        GlobalController.getInstance().addTabStatic(SALES_INVOICE_CREATE_SLUG, false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ResponsiveWiseCssPicker();

        //todo : autofocus on Search
        Platform.runLater(() -> tfSalesRtnSearch.requestFocus());
        //         Enter traversal
        bpSalesRtnRootPane.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
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

        initSalesInvoiceListTable();
    }

    private void ResponsiveWiseCssPicker() {
        double height = Screen.getPrimary().getBounds().getHeight();
        double width = Screen.getPrimary().getBounds().getWidth();
        System.out.println("width" + width);
        if (width >= 992 && width <= 1024) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle1.css").toExternalForm());
        } else if (width >= 1025 && width <= 1280) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle2.css").toExternalForm());
        } else if (width >= 1281 && width <= 1368) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle3.css").toExternalForm());
        } else if (width >= 1369 && width <= 1400) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle4.css").toExternalForm());
        } else if (width >= 1401 && width <= 1440) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle5.css").toExternalForm());
        } else if (width >= 1441 && width <= 1680) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle6.css").toExternalForm());
        } else if (width >= 1681 && width <= 1920) {
            bpSalesRtnRootPane.getStylesheets().add(GenivisApplication.class.getResource("ui/css/TranxCommonCssStyles/tranxCommonCssStyle7.css").toExternalForm());
        }
    }

    private void initSalesInvoiceListTable() {
        getSalesInvoiceList("");
        tcInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        tcTranxId.setCellValueFactory(new PropertyValueFactory<>("tranxCode"));
        tcSIDate.setCellValueFactory(new PropertyValueFactory<>("invoiceDate"));
        tcClientName.setCellValueFactory(new PropertyValueFactory<>("sundryDebtorName"));
        tcNarration.setCellValueFactory(new PropertyValueFactory<>("narration"));
        tcTaxable.setCellValueFactory(new PropertyValueFactory<>("taxableAmt"));
        tcTax.setCellValueFactory(new PropertyValueFactory<>("taxAmt"));
        tcBillAmt.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tcPayMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
        tcAction.setCellFactory(param -> {
            final TableCell<TranxSalesInvoiceListMDL, Void> cell = new TableCell<>() {
                private ImageView delImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/del.png"))));
                private ImageView edtImg = new ImageView(new Image(String.valueOf(GenivisApplication.class.getResource("ui/assets/edt.png"))));

                {
                    delImg.setFitHeight(20.0);
                    delImg.setFitWidth(20.0);
                    edtImg.setFitHeight(20.0);
                    edtImg.setFitWidth(20.0);

                }

                private final Button actionButton = new Button("", delImg);
                private final Button edtButton = new Button("", edtImg);


                {
                    edtButton.setOnAction(actionEvent -> {
                        Integer id = lstSalesInvoice.get(getIndex()).getId();
                        GlobalController.getInstance().addTabStaticWithParam(SALES_INVOICE_RTN_EDIT_SLUG, false, id);
                    });
                }

                HBox hbActions = new HBox();

                {
                    hbActions.getChildren().add(edtButton);
                    hbActions.getChildren().add(actionButton);
                    hbActions.setSpacing(10.0);
                }

                // Set the action for the view button
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
        tvSalesInvoiceList.setItems(lstSalesInvoice);
    }

    private void getSalesInvoiceList(String searchKey) {
        JSONObject jsonSort = new JSONObject();
        jsonSort.put("colId", "null");
        jsonSort.put("isAsc", "true");
        Map<String, String> body = new HashMap<>();
        body.put("pageNo", "1");
        body.put("pageSize", "50");
        body.put("searchText", searchKey);
        body.put("sort", "");
        body.put("startDate", "");
        body.put("endDate", "");
        String requestBody = Globals.mapToString(body);
        HttpResponse<String> response = APIClient.postJsonRequest(requestBody, EndPoints.TRANX_SALES_INVOICE_RTN_LIST);
        JSONObject resObj = new JSONObject(response.body());
        lstSalesInvoice.clear();
        if (resObj.getInt("responseStatus") == 200) {
            JSONArray jArr = resObj.getJSONObject("responseObject").getJSONArray("data");
            for (Object object : jArr) {
                TranxSalesInvoiceRtnListResDTO tranxSalesInvoiceListResDTO = new Gson().fromJson(object.toString(), TranxSalesInvoiceRtnListResDTO.class);
                lstSalesInvoice.add(new TranxSalesInvoiceListMDL(tranxSalesInvoiceListResDTO.getId(), tranxSalesInvoiceListResDTO.getPaymentMode(), tranxSalesInvoiceListResDTO.getTransactionTrackingNo(), tranxSalesInvoiceListResDTO.getReferenceNo(), tranxSalesInvoiceListResDTO.getReferenceType(), tranxSalesInvoiceListResDTO.getInvoiceNo(), tranxSalesInvoiceListResDTO.getTaxableAmt(), tranxSalesInvoiceListResDTO.getTranxCode(), DateConvertUtil.convertDispDateFormat(tranxSalesInvoiceListResDTO.getInvoiceDate()), tranxSalesInvoiceListResDTO.getSaleSerialNumber(), tranxSalesInvoiceListResDTO.getSundryDebtorId(), tranxSalesInvoiceListResDTO.getSundryDebtorName(), tranxSalesInvoiceListResDTO.getTaxAmt(), tranxSalesInvoiceListResDTO.getNarration(), tranxSalesInvoiceListResDTO.getTotalIgst(), tranxSalesInvoiceListResDTO.getTotalCgst(), tranxSalesInvoiceListResDTO.getTotalSgst(), tranxSalesInvoiceListResDTO.getSaleAccountName(), tranxSalesInvoiceListResDTO.getTotalAmount()));
            }
        }
    }
}
