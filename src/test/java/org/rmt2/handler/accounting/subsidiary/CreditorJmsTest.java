package org.rmt2.handler.accounting.subsidiary;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.util.List;

import org.dto.CreditorDto;
import org.dto.CreditorXactHistoryDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.subsidiary.CreditorApi;
import org.modules.subsidiary.CreditorApiException;
import org.modules.subsidiary.SubsidiaryApiFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.AccountingMockData;
import org.rmt2.api.handlers.subsidiary.CreditorApiHandler;
import org.rmt2.handler.BaseMockSingleConsumerMDBTest;

import com.api.messaging.jms.JmsClientManager;
import com.api.messaging.webservice.WebServiceConstants;
import com.api.util.RMT2File;



/**
 * Test the idenity and invocation of the Creditor API Message Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ JmsClientManager.class, CreditorApiHandler.class, SubsidiaryApiFactory.class })
public class CreditorJmsTest extends BaseMockSingleConsumerMDBTest {

    private static final String DESTINATION = "rmt2.queue.accounting";
    private CreditorApi mockApi;


    /**
     * 
     */
    public CreditorJmsTest() {
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
        this.mockApi = Mockito.mock(CreditorApi.class);
        PowerMockito.mockStatic(SubsidiaryApiFactory.class);
        when(SubsidiaryApiFactory.createCreditorApi(isA(String.class))).thenReturn(mockApi);
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
    public void invokeHandlerSuccess_FetchCreditors() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/subsidiary/CreditorQueryRequest.xml");
        List<CreditorDto> mockListData = AccountingMockData.createMockCreditors();
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.getExt(isA(CreditorDto.class))).thenReturn(mockListData);
        } catch (CreditorApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).getExt(isA(CreditorDto.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    

    @Test
    public void invokeHandlerSuccess_FetchCreditorTransactionHistory() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/subsidiary/CreditorTransHistQueryRequest.xml");
        List<CreditorDto> mockCredData = AccountingMockData.createMockCreditor();
        List<CreditorXactHistoryDto> mockListData = AccountingMockData.createMockCreditorXactHistory();
        this.setupMocks(DESTINATION, request);
        
        try {
            when(this.mockApi.getExt(isA(CreditorDto.class))).thenReturn(mockCredData);
        } catch (CreditorApiException e) {
            Assert.fail("Unable to setup mock stub for fetching a creditor");
        }
        try {
            when(this.mockApi.getTransactionHistory(isA(Integer.class))).thenReturn(mockListData);
        } catch (CreditorApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).getTransactionHistory(isA(Integer.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    
    @Test
    public void invokeHandlerSuccess_UpdateCreditor() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/subsidiary/CreditorUpdateRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.update(isA(CreditorDto.class))).thenReturn(WebServiceConstants.RETURN_CODE_SUCCESS);
        } catch (CreditorApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).update(isA(CreditorDto.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    
    @Test
    public void invokeHandlerSuccess_DeleteCreditor() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/subsidiary/CreditorDeleteRequest.xml");
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.delete(isA(CreditorDto.class))).thenReturn(WebServiceConstants.RETURN_CODE_SUCCESS);
        } catch (CreditorApiException e) {
            e.printStackTrace();
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).delete(isA(CreditorDto.class));
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
    }
    
    @Test
    public void invokeHandlerError_Fetch_Incorrect_Trans_Code() {
        String request = RMT2File.getFileContentsAsString("xml/accounting/subsidiary/CreditorHandlerInvalidTransCodeRequest.xml");
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
