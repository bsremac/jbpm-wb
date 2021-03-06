/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workbench.pr.client.editors.instance.log;

import java.util.Date;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.list.DataSetQueryHelper;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.service.TaskService;
import org.jbpm.workbench.pr.client.util.LogUtils;
import org.jbpm.workbench.pr.events.ProcessInstanceSelectionEvent;
import org.jbpm.workbench.pr.model.ProcessInstanceLogSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.mocks.CallerMock;

import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.COLUMN_LOG_DATE;
import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.COLUMN_LOG_ID;
import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.COLUMN_LOG_NODE_NAME;
import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.COLUMN_LOG_NODE_TYPE;
import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.COLUMN_LOG_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class ProcessInstanceLogPresenterTest {

    @Mock
    ProcessInstanceLogPresenter.ProcessInstanceLogView view;

    @Mock
    protected ErrorPopupPresenter errorPopup;

    @Mock
    protected DataSetQueryHelper dataSetQueryHelper;

    @Mock
    protected ProcessInstanceLogFilterSettingsManager filterSettingsManager;

    @Mock
    FilterSettings currentFilterSettings;

    @Mock
    private DataSet dataSet;

    @Mock
    TaskService taskServiceMock;

    Caller<TaskService> taskService;

    @InjectMocks
    ProcessInstanceLogPresenter presenter;

    private String processName = "processName";
    private String testTask = "testTask";
    private String datasetUID = "jbpmProcessInstanceLogs";

    private Date logDate = new Date();

    private Long[] pilIds = new Long[4];
    private String[] pilNodeType = new String[4];
    private String[] pilNodeNames = new String[4];
    private Boolean[] pilCompleted = new Boolean[4];

    @Before
    public void setup() {
        pilIds[0] = 1L;
        pilIds[1] = 1L + 1;
        pilIds[2] = 1L + 2;
        pilIds[3] = 1l + 3;

        pilNodeNames[0] = "";
        pilNodeNames[1] = testTask;
        pilNodeNames[2] = "";
        pilNodeNames[3] = "";

        pilNodeType[0] = LogUtils.NODE_START;
        pilNodeType[1] = LogUtils.NODE_HUMAN_TASK;
        pilNodeType[2] = LogUtils.NODE_END;
        pilNodeType[3] = "Split";

        pilCompleted[0] = false;
        pilCompleted[1] = false;
        pilCompleted[2] = true;
        pilCompleted[3] = false;

        for (int i = 0; i < pilIds.length; i++) {
            defineDatasetAnswer(i,
                                pilIds[i],
                                logDate,
                                pilNodeNames[i],
                                pilNodeType[i],
                                pilCompleted[i]);
        }

        when(dataSet.getRowCount()).thenReturn(4);
        when(dataSet.getUUID()).thenReturn(datasetUID);

        presenter.setDataSetQueryHelper(dataSetQueryHelper);
        presenter.setFilterSettingsManager(filterSettingsManager);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((DataSetReadyCallback) invocation.getArguments()[1]).callback(dataSet);
                return null;
            }
        }).when(dataSetQueryHelper).lookupDataSet(anyInt(),
                                                  any(DataSetReadyCallback.class));

        when(dataSetQueryHelper.getCurrentTableSettings()).thenReturn(currentFilterSettings);
        when(filterSettingsManager.createDefaultFilterSettingsPrototype(anyLong())).thenReturn(currentFilterSettings);
        when(currentFilterSettings.getKey()).thenReturn("key");
        when(currentFilterSettings.getDataSet()).thenReturn(dataSet);

        taskService = new CallerMock<>(taskServiceMock);
        presenter.setTaskService(taskService);
    }

    private void assertProcessInstanceLogContent(Long id,
                                                 Date date,
                                                 String nodeName,
                                                 String nodeType,
                                                 boolean completed,
                                                 ProcessInstanceLogSummary processInstanceLogSummaryDest) {
        assertEquals(id,
                     processInstanceLogSummaryDest.getId());
        assertEquals(date,
                     processInstanceLogSummaryDest.getDate());
        assertEquals(nodeName,
                     processInstanceLogSummaryDest.getName());
        assertEquals(nodeType,
                     processInstanceLogSummaryDest.getNodeType());
        assertEquals(completed,
                     processInstanceLogSummaryDest.isCompleted());
    }

    public void defineDatasetAnswer(int position,
                                    Long id,
                                    Date date,
                                    String nodeName,
                                    String nodeType,
                                    boolean completed) {
        when(dataSet.getValueAt(position,
                                COLUMN_LOG_ID)).thenReturn(id);
        when(dataSet.getValueAt(position,
                                COLUMN_LOG_DATE)).thenReturn(date);
        when(dataSet.getValueAt(position,
                                COLUMN_LOG_NODE_NAME)).thenReturn(nodeName);
        when(dataSet.getValueAt(position,
                                COLUMN_LOG_NODE_TYPE)).thenReturn(nodeType);
        when(dataSet.getValueAt(position,
                                COLUMN_LOG_TYPE)).thenReturn((completed ? 1 : 0));
    }

    @Test
    public void datasetLookupNotFoundTest() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((DataSetReadyCallback) invocation.getArguments()[1]).notFound();
                ;
                return null;
            }
        }).when(dataSetQueryHelper).lookupDataSet(anyInt(),
                                                  any(DataSetReadyCallback.class));
        presenter.loadProcessInstanceLogs();
        verify(errorPopup).showMessage(eq(org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE.DataSetNotFound(datasetUID)));
    }

    @Test
    public void datasetLookupErrorTest() {
        String errorMessage = "error message";
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((DataSetReadyCallback) invocation.getArguments()[1]).onError(new ClientRuntimeError(errorMessage));
                return null;
            }
        }).when(dataSetQueryHelper).lookupDataSet(anyInt(),
                                                  any(DataSetReadyCallback.class));
        presenter.loadProcessInstanceLogs();
        verify(errorPopup).showMessage(eq(org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE.DataSetError(datasetUID,
                                                                                                                          errorMessage)));
    }

    @Test
    public void onProcessInstaceSelectionTest() {

        Long processInstanceId = 1L;
        String processDefId = "processDefId";
        String deploymentId = "deploymentId";
        Integer processInstanceStatus = 0;
        String serverTemplateId = "serverTemplateId";

        presenter.onProcessInstanceSelectionEvent(new ProcessInstanceSelectionEvent(deploymentId,
                                                                                    processInstanceId,
                                                                                    processDefId,
                                                                                    processName,
                                                                                    processInstanceStatus,
                                                                                    serverTemplateId));

        verify(filterSettingsManager).createDefaultFilterSettingsPrototype(eq(processInstanceId));
        verify(dataSetQueryHelper,
               times(2)).setCurrentTableSettings(filterSettingsManager.createDefaultFilterSettingsPrototype(processInstanceId));

        dataSetQueryHelper.setCurrentTableSettings(filterSettingsManager.createDefaultFilterSettingsPrototype(processInstanceId));

        ArgumentCaptor<List> argumentDESC = ArgumentCaptor.forClass(List.class);
        verify(view).setLogsList(argumentDESC.capture());
        assertEquals(4,
                     argumentDESC.getValue().size());

        for (int i = 0; i < argumentDESC.getValue().size(); i++) {
            assertProcessInstanceLogContent(pilIds[i],
                                            logDate,
                                            pilNodeNames[i],
                                            pilNodeType[i],
                                            pilCompleted[i],
                                            (ProcessInstanceLogSummary) argumentDESC.getValue().get(i));
        }
    }

    @Test
    public void testLoadMoreProcessInstanceLogs() {
        presenter.setCurrentPage(0);
        presenter.loadProcessInstanceLogs();
        assertEquals(0,
                     presenter.getCurrentPage());
        verify(dataSetQueryHelper).lookupDataSet(eq(presenter.getPageSize() * presenter.getCurrentPage()),
                                                 any());
        ArgumentCaptor<List> argumentDESC = ArgumentCaptor.forClass(List.class);
        verify(view).setLogsList(argumentDESC.capture());
        assertEquals(4,
                     argumentDESC.getValue().size());
        verify(view).hideLoadButton(true);

        presenter.loadMoreProcessInstanceLogs();

        assertEquals(1,
                     presenter.getCurrentPage());
        verify(dataSetQueryHelper).lookupDataSet(eq(presenter.getPageSize() * presenter.getCurrentPage()),
                                                 any());
        verify(view,
               times(2)).setLogsList(argumentDESC.capture());
        assertEquals(8,
                     argumentDESC.getValue().size());

        verify(view,
               times(2)).setLogsList(anyList());
    }

    @Test
    public void testLoadTaskDetails() {
        Long workItemId = 1L;
        String containerId = "deploymentId";
        String serverTemplateId = "server-template-id";

        TaskSummary task = TaskSummary.builder().id(workItemId).actualOwner("owner").createdOn(new Date()).description("description").build();
        ProcessInstanceLogHumanTaskView humanTaskView = mock(ProcessInstanceLogHumanTaskView.class);
        when(taskServiceMock.getTaskByWorkItemId(serverTemplateId,
                                                 containerId,
                                                 workItemId)).thenReturn(task);

        ProcessInstanceSelectionEvent selectionEventMock = mock(ProcessInstanceSelectionEvent.class);
        when(selectionEventMock.getDeploymentId()).thenReturn(containerId);
        when(selectionEventMock.getServerTemplateId()).thenReturn(serverTemplateId);

        presenter.onProcessInstanceSelectionEvent(selectionEventMock);
        presenter.loadTaskDetails(workItemId,
                                  logDate,
                                  humanTaskView);

        verify(taskServiceMock).getTaskByWorkItemId(serverTemplateId,
                                                    containerId,
                                                    workItemId);
        verify(humanTaskView).setDetailsData(task,
                                             logDate);
    }


}
