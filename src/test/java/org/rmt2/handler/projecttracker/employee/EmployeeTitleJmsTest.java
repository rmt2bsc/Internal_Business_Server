package org.rmt2.handler.projecttracker.employee;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.dto.EmployeeTitleDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modules.ProjectTrackerApiConst;
import org.modules.employee.EmployeeApi;
import org.modules.employee.EmployeeApiException;
import org.modules.employee.EmployeeApiFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rmt2.api.handlers.employee.title.EmployeeTitleApiHandler;
import org.rmt2.handler.BaseMockSingleConsumerMDBTest;
import org.rmt2.handler.projecttracker.ProjectTrackerJmsMockData;

import com.api.messaging.jms.JmsClientManager;
import com.api.util.RMT2File;



/**
 * Test the idenity and invocation of the Employee Title related JMS messages
 * for the Project Tracker API Message Handler.
 * 
 * @author appdev
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ EmployeeTitleApiHandler.class, JmsClientManager.class, EmployeeApiFactory.class })
public class EmployeeTitleJmsTest extends BaseMockSingleConsumerMDBTest {

    private static final String DESTINATION = "rmt2.queue.projecttracker";
    private EmployeeApi mockApi;


    /**
     * 
     */
    public EmployeeTitleJmsTest() {
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

        this.mockApi = Mockito.mock(EmployeeApi.class);
        PowerMockito.mockStatic(EmployeeApiFactory.class);
        when(EmployeeApiFactory.createApi(eq(ProjectTrackerApiConst.APP_NAME))).thenReturn(this.mockApi);
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
    public void invokeHandelrSuccess_Fetch() {
        String request = RMT2File.getFileContentsAsString("xml/projecttracker/employee/EmployeeTitleQueryRequest.xml");
        List<EmployeeTitleDto> apiResults = ProjectTrackerJmsMockData.createMockEmployeeTitle();
        this.setupMocks(DESTINATION, request);
        try {
            when(this.mockApi.getEmployeeTitles()).thenReturn(apiResults);
        } catch (EmployeeApiException e) {
            e.printStackTrace();
            Assert.fail("Employee title fetch test case failed");
        }

        try {
            this.startTest();    
            Mockito.verify(this.mockApi).getEmployeeTitles();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An unexpected exception was thrown");
        }
        
    }

}
