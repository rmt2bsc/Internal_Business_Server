package org.rmt2.handler.accounting.transaction.sales;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.dto.ContactDto;
import org.dto.CustomerDto;
import org.dto.SalesInvoiceDto;
import org.dto.SalesOrderItemDto;
import org.dto.XactDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.contacts.ContactsApi;
import org.modules.contacts.ContactsApiException;
import org.modules.contacts.ContactsApiFactory;
import org.modules.subsidiary.CustomerApi;
import org.modules.subsidiary.CustomerApiException;
import org.modules.subsidiary.SubsidiaryApiFactory;
import org.modules.transaction.XactApi;
import org.modules.transaction.XactApiException;
import org.modules.transaction.XactApiFactory;
import org.modules.transaction.sales.SalesApi;
import org.modules.transaction.sales.SalesApiFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.api.handlers.transaction.sales.PrintCustomerSalesOrderApiHandler;
import org.rmt2.handler.BaseMockSingleConsumerMDBTest;
import org.rmt2.handler.accounting.transaction.TransactionDatasourceMock;

import com.api.messaging.jms.JmsClientManager;
import com.api.util.RMT2File;

/**
 * Test the idenity and invocation of the sales order create message handler
 * Message Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ JmsClientManager.class, XactApiFactory.class, PrintCustomerSalesOrderApiHandler.class, SalesApiFactory.class,
        SubsidiaryApiFactory.class, ContactsApiFactory.class, XactApiFactory.class })
public class SalesOrderPrintJmsTest extends BaseMockSingleConsumerMDBTest {
    public static final int SALES_ORDER_ID = 1000;
    public static final int CUSTOMER_ID = 3333;
    public static final double TEST_ORDER_TOTAL = 300;
    private static final String DESTINATION = "rmt2.queue.accounting";
    public static final String PROP_SERIAL_PATH = "\\temp\\output\\";
    private SalesApi mockSalesApi;
    private CustomerApi mockCustApi;
    private ContactsApi mockContactApi;
    private XactApi mockXactApi;
    private String path;

    public static final int NEW_XACT_ID = 1234567;

    /**
     * 
     */
    public SalesOrderPrintJmsTest() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see testcases.messaging.MessageToListenerToHandlerTest#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Setup Transaction Datasource mocks
        TransactionDatasourceMock.setupXactMocks();

        // Setup API Mocks
        mockSalesApi = Mockito.mock(SalesApi.class);
        mockCustApi = Mockito.mock(CustomerApi.class);
        mockContactApi = Mockito.mock(ContactsApi.class);
        mockXactApi = Mockito.mock(XactApi.class);
        PowerMockito.mockStatic(SalesApiFactory.class);
        PowerMockito.mockStatic(SubsidiaryApiFactory.class);
        PowerMockito.mockStatic(ContactsApiFactory.class);
        PowerMockito.mockStatic(XactApiFactory.class);
        PowerMockito.when(SalesApiFactory.createApi()).thenReturn(this.mockSalesApi);
        PowerMockito.when(SubsidiaryApiFactory.createCustomerApi()).thenReturn(this.mockCustApi);
        PowerMockito.when(ContactsApiFactory.createApi()).thenReturn(this.mockContactApi);
        PowerMockito.when(XactApiFactory.createDefaultXactApi()).thenReturn(this.mockXactApi);
        doNothing().when(this.mockSalesApi).close();

        // Setup System Properteis
        System.setProperty("SerialPath", PROP_SERIAL_PATH);
        System.setProperty("SerialDrive", "c:");
        System.setProperty("RptXsltPath", "reports");
        System.setProperty("CompContactId", "7777");
        System.setProperty("CompanyName", "XYZ Company");
        System.setProperty("CompContactFirstname", "test");
        System.setProperty("CompContactLastname", "user");
        System.setProperty("CompContactPhone", "999.999.9999");
        System.setProperty("CompContactEmail", "test.user@xyz.com");
        System.setProperty("CompTaxId", "449999999");
        System.setProperty("CompWebsite", "www.xya.com");
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

        String homeDir = System.getProperty("user.home");
        homeDir = RMT2File.convertToUnixStyle(homeDir);
        String pathExt = RMT2File.convertToUnixStyle(PROP_SERIAL_PATH);
        path = homeDir + pathExt;
        File f = new File(path);
        if (RMT2File.verifyDirectory(f) == RMT2File.FILE_IO_NOTEXIST) {
            RMT2File.createDirectory(path);
        }
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see testcases.messaging.MessageToListenerToHandlerTest#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        File f = new File(path);
        RMT2File.deleteFile(f);
        return;
    }

    @Test
    public void invokeHandler_Success() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/transaction/sales/SalesOrderPrintRequest.xml");
        
        List<SalesInvoiceDto> mockSalesOrderDtoList = SalesOrderJmsMockData.createMockSalesInvoice();
        List<SalesOrderItemDto> mockSalesOrderItems = SalesOrderJmsMockData.createMockSalesOrderItems(SALES_ORDER_ID);
        List<CustomerDto> mockCustomerListData = SalesOrderJmsMockData.createMockCustomer();
        mockCustomerListData.get(0).setCustomerId(CUSTOMER_ID);
        List<ContactDto> mockBusinessContactDtoList = SalesOrderJmsMockData.createMockSingleBusinessContactDto();
        List<XactDto> mockXactListData = SalesOrderJmsMockData.createMockSingleCommonTransactions();
        mockXactListData.get(0).setXactAmount(TEST_ORDER_TOTAL);
        
        try {
            when(this.mockSalesApi.getInvoice(isA(SalesInvoiceDto.class))).thenReturn(mockSalesOrderDtoList);
        } catch (Exception e) {
            Assert.fail("Unable to setup mock stub for fetching sales order DTO list");
        }

        try {
            when(this.mockSalesApi.getLineItems(eq(SALES_ORDER_ID))).thenReturn(mockSalesOrderItems);
        } catch (Exception e) {
            Assert.fail("Unable to setup mock stub for fetching sales order item DTO list for Sales Order Id 1000");
        }

        try {
            when(this.mockCustApi.getExt(isA(CustomerDto.class))).thenReturn(mockCustomerListData);
        } catch (CustomerApiException e) {
            Assert.fail("Unable to setup mock stub for fetching a customer");
        }

        try {
            when(this.mockContactApi.getContact(isA(ContactDto.class))).thenReturn(mockBusinessContactDtoList);
        } catch (ContactsApiException e) {
            Assert.fail("Unable to setup mock stub for fetching a list of contacts");
        }

        try {
            when(this.mockXactApi.getXact(isA(XactDto.class))).thenReturn(mockXactListData);
        } catch (XactApiException e) {
            Assert.fail("Unable to setup mock stub for fetching a BASE transaction");
        }
        
        this.setupMocks(DESTINATION, request);


        try {
            this.startTest();
            Mockito.verify(this.mockSalesApi).getInvoice(isA(SalesInvoiceDto.class));
            Mockito.verify(this.mockCustApi).getExt(isA(CustomerDto.class));
            Mockito.verify(this.mockContactApi, times(2)).getContact(isA(ContactDto.class));
            Mockito.verify(this.mockXactApi).getXact(isA(XactDto.class));
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }

    @Test
    public void invokeHandlerError_Incorrect_Trans_Code() {
        String request = RMT2File
                .getFileContentsAsString("xml/accounting/transaction/sales/SalesOrderCreateInvalidTransCodeRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            this.startTest();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }

}
