package org.rmt2.handler.accounting;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.List;

import org.dto.CustomerDto;
import org.dto.CustomerXactHistoryDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.subsidiary.CustomerApi;
import org.modules.subsidiary.CustomerApiException;
import org.modules.subsidiary.SubsidiaryApiFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.AccountingMockData;
import org.rmt2.BaseMockMessageDrivenBeanTest;
import org.rmt2.api.handlers.subsidiary.CustomerApiHandler;

import com.api.messaging.jms.JmsClientManager;
import com.api.messaging.webservice.WebServiceConstants;
import com.api.util.RMT2File;



/**
 * Test the idenity and invocation of the Customer API Message Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ JmsClientManager.class, CustomerApiHandler.class, SubsidiaryApiFactory.class })
public class CustomerJmsTest extends BaseMockMessageDrivenBeanTest {

    private static final String DESTINATION = "Test-Accounting-Queue";
    private SubsidiaryApiFactory mockApiFactory;
    private CustomerApi mockApi;


    /**
     * 
     */
    public CustomerJmsTest() {
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
        this.mockApiFactory = Mockito.mock(SubsidiaryApiFactory.class);
        this.mockApi = Mockito.mock(CustomerApi.class);
        try {
            whenNew(SubsidiaryApiFactory.class).withNoArguments().thenReturn(this.mockApiFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(mockApiFactory.createCustomerApi(isA(String.class))).thenReturn(mockApi);
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see testcases.messaging.MessageToListenerToHandlerTest#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        return;
    }

    @Test
    public void invokeHandlerSuccess_FetchCustomers() {
        String request = RMT2File.getFileContentsAsString("xml/subsidiary/CustomerQueryRequest.xml");
        List<CustomerDto> mockListData = AccountingMockData.createMockCustomers();
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.getExt(isA(CustomerDto.class))).thenReturn(mockListData);
        } catch (CustomerApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    

    @Test
    public void invokeHandlerSuccess_FetchCustomerTransactionHistory() {
        String request = RMT2File.getFileContentsAsString("xml/subsidiary/CustomerTranHistQueryRequest.xml");
        List<CustomerXactHistoryDto> mockListData = AccountingMockData.createMockCustomerXactHistory();
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.getTransactionHistory(isA(Integer.class))).thenReturn(mockListData);
        } catch (CustomerApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    
    @Test
    public void invokeHandlerSuccess_UpdateCustomer() {
        String request = RMT2File.getFileContentsAsString("xml/subsidiary/CustomerUpdateRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.update(isA(CustomerDto.class))).thenReturn(WebServiceConstants.RETURN_CODE_SUCCESS);
        } catch (CustomerApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).update(isA(CustomerDto.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    
    @Test
    public void invokeHandlerError_Fetch_Incorrect_Trans_Code() {
        String request = RMT2File.getFileContentsAsString("xml/subsidiary/CustomerHandlerInvalidTransCodeRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            this.startTest();    
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
  
}
