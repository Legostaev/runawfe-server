/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.logic;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.timer.ScheduledTimerTask;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.DatabaseProperties;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dao.ConstantDAO;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.commons.dbpatch.UnsupportedPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddAggregatedTaskIndexPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddAssignDateColumnPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddBatchPresentationIsSharedPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnForEmbeddedBotTaskFileName;
import ru.runa.wfe.commons.dbpatch.impl.AddColumnsToSubstituteEscalatedTasksPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddCreateDateColumns;
import ru.runa.wfe.commons.dbpatch.impl.AddDeploymentAuditPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddDueDateExpressionToJobAndTask;
import ru.runa.wfe.commons.dbpatch.impl.AddEmbeddedFileForBotTask;
import ru.runa.wfe.commons.dbpatch.impl.AddHierarchyProcess;
import ru.runa.wfe.commons.dbpatch.impl.AddMultiTaskIndexToTaskPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddNodeIdToProcessLogPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddParentProcessIdPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddProcessAndTokenExecutionStatusPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddSequentialFlagToBot;
import ru.runa.wfe.commons.dbpatch.impl.AddSettingsTable;
import ru.runa.wfe.commons.dbpatch.impl.AddSubProcessIndexColumn;
import ru.runa.wfe.commons.dbpatch.impl.AddTitleAndDepartmentColumnsToActorPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenErrorDataPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddTokenMessageHashPatch;
import ru.runa.wfe.commons.dbpatch.impl.AddVariableUniqueKeyPatch;
import ru.runa.wfe.commons.dbpatch.impl.CreateAdminScriptTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateAggregatedLogsTables;
import ru.runa.wfe.commons.dbpatch.impl.CreateReportsTables;
import ru.runa.wfe.commons.dbpatch.impl.ExpandDescriptionsPatch;
import ru.runa.wfe.commons.dbpatch.impl.ExpandVarcharPatch;
import ru.runa.wfe.commons.dbpatch.impl.JbpmRefactoringPatch;
import ru.runa.wfe.commons.dbpatch.impl.NodeTypeChangePatch;
import ru.runa.wfe.commons.dbpatch.impl.PerformancePatch401;
import ru.runa.wfe.commons.dbpatch.impl.PermissionMappingPatch403;
import ru.runa.wfe.commons.dbpatch.impl.TaskCreateLogSeverityChangedPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskEndDateRemovalPatch;
import ru.runa.wfe.commons.dbpatch.impl.TaskOpenedByExecutorsPatch;
import ru.runa.wfe.commons.dbpatch.impl.TransitionLogPatch;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.job.impl.JobTask;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;

/**
 * Initial DB population and update during version change.
 * 
 * @author Dofs
 */
public class InitializerLogic {
    protected static final Log log = LogFactory.getLog(InitializerLogic.class);

    public static final List<Class<? extends DBPatch>> dbPatches;

    static {
        List<Class<? extends DBPatch>> patches = Lists.newArrayList();
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        patches.add(UnsupportedPatch.class);
        // version 20
        // 4.0.0
        patches.add(AddHierarchyProcess.class);
        patches.add(JbpmRefactoringPatch.class);
        patches.add(TransitionLogPatch.class);
        // 4.0.1
        patches.add(PerformancePatch401.class);
        patches.add(TaskEndDateRemovalPatch.class);
        // 4.0.1
        patches.add(PermissionMappingPatch403.class);
        // 4.0.5
        patches.add(NodeTypeChangePatch.class);
        patches.add(ExpandDescriptionsPatch.class);
        // 4.0.6
        patches.add(TaskOpenedByExecutorsPatch.class);
        // 4.1.0
        patches.add(AddNodeIdToProcessLogPatch.class);
        patches.add(AddSubProcessIndexColumn.class);
        // 4.1.1
        patches.add(AddCreateDateColumns.class);
        // 4.2.0
        patches.add(AddEmbeddedFileForBotTask.class);
        patches.add(AddColumnForEmbeddedBotTaskFileName.class);
        patches.add(AddSettingsTable.class);
        patches.add(AddSequentialFlagToBot.class);
        patches.add(CreateAggregatedLogsTables.class);
        patches.add(TaskCreateLogSeverityChangedPatch.class);
        patches.add(AddColumnsToSubstituteEscalatedTasksPatch.class);
        // 4.2.1
        patches.add(AddMultiTaskIndexToTaskPatch.class);
        // 4.2.2
        patches.add(AddDeploymentAuditPatch.class);
        // 4.3.0
        patches.add(AddAggregatedTaskIndexPatch.class);
        patches.add(AddParentProcessIdPatch.class);
        patches.add(CreateReportsTables.class);
        patches.add(AddDueDateExpressionToJobAndTask.class);
        patches.add(AddBatchPresentationIsSharedPatch.class);
        patches.add(ExpandVarcharPatch.class);
        patches.add(AddProcessAndTokenExecutionStatusPatch.class);
        patches.add(CreateAdminScriptTables.class);
        patches.add(AddVariableUniqueKeyPatch.class);
        patches.add(AddTokenErrorDataPatch.class);
        patches.add(AddTitleAndDepartmentColumnsToActorPatch.class);
        patches.add(AddAssignDateColumnPatch.class);
        patches.add(AddTokenMessageHashPatch.class);
        dbPatches = Collections.unmodifiableList(patches);
    };

    @Autowired
    private ConstantDAO constantDAO;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private LocalizationDAO localizationDAO;
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    /**
     * Initialize database if needed.
     */
    public void onStartup(UserTransaction transaction) {
        try {
            Integer databaseVersion = constantDAO.getDatabaseVersion();
            if (databaseVersion != null) {
                applyPatches(transaction, databaseVersion);
            } else {
                initializeDatabase(transaction);
            }
            permissionDAO.init();
            postProcessPatches(transaction);
            String localizedFileName = "localizations." + Locale.getDefault().getLanguage() + ".xml";
            InputStream stream = ClassLoaderUtil.getAsStream(localizedFileName, getClass());
            if (stream == null) {
                stream = ClassLoaderUtil.getAsStreamNotNull("localizations.xml", getClass());
            }
            List<Localization> localizations = LocalizationParser.parseLocalizations(stream);
            stream = ClassLoaderUtil.getAsStream(SystemProperties.RESOURCE_EXTENSION_PREFIX + localizedFileName, getClass());
            if (stream == null) {
                stream = ClassLoaderUtil.getAsStream(SystemProperties.RESOURCE_EXTENSION_PREFIX + "localizations.xml", getClass());
            }
            if (stream != null) {
                localizations.addAll(LocalizationParser.parseLocalizations(stream));
            }
            localizationDAO.saveLocalizations(localizations, false);
            if (DatabaseProperties.isDynamicSettingsEnabled()) {
                PropertyResources.setDatabaseAvailable(true);
            }
            setScheduledTaskTimerSettings();
            JobTask.setSystemStartupCompleted(true);
        } catch (Exception e) {
            log.error("initialization failed", e);
        }
    }

    @SuppressWarnings("deprecation")
    private void setScheduledTaskTimerSettings() {
        ApplicationContext context = ApplicationContextFactory.getContext();
        PropertyResources resources = SystemProperties.getResources();
        ScheduledTimerTask jobExecutorTask = context.getBean("jobExecutorTask", ScheduledTimerTask.class);
        jobExecutorTask.setDelay(resources.getLongProperty(SystemProperties.TIMERTASK_START_MILLIS_JOB_EXECUTION_NAME, 60000));
        jobExecutorTask.setPeriod(resources.getLongProperty(SystemProperties.TIMERTASK_PERIOD_MILLIS_JOB_EXECUTION_NAME, 60000));
        ScheduledTimerTask tasksAssignTask = context.getBean("tasksAssignTask", ScheduledTimerTask.class);
        tasksAssignTask.setDelay(resources.getLongProperty(SystemProperties.TIMERTASK_START_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME, 60000));
        tasksAssignTask.setPeriod(resources.getLongProperty(SystemProperties.TIMERTASK_PERIOD_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME, 60000));
    }

    /**
     * Initialize database.
     * 
     * @param daoHolder
     *            Helper object for getting DAO's.
     */
    private void initializeDatabase(UserTransaction transaction) {
        log.info("database is not initialized. initializing...");
        SchemaExport schemaExport = new SchemaExport(ApplicationContextFactory.getConfiguration());
        schemaExport.execute(true, true, false, true);
        try {
            transaction.begin();
            insertInitialData();
            constantDAO.setDatabaseVersion(dbPatches.size());
            transaction.commit();
        } catch (Throwable th) {
            Utils.rollbackTransaction(transaction);
            log.info("unable to insert initial data", th);
        }
    }

    /**
     * Inserts initial data on database creation stage
     */
    private void insertInitialData() {
        // create privileged Executors
        String administratorName = SystemProperties.getAdministratorName();
        Actor admin = new Actor(administratorName, administratorName, administratorName);
        admin = executorDAO.create(admin);
        executorDAO.setPassword(admin, SystemProperties.getAdministratorDefaultPassword());
        String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
        Group adminGroup = executorDAO.create(new Group(administratorsGroupName, administratorsGroupName));
        executorDAO.create(new Group(SystemProperties.getBotsGroupName(), SystemProperties.getBotsGroupName()));
        List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
        executorDAO.addExecutorToGroup(admin, adminGroup);
        executorDAO.create(new Actor(SystemExecutors.PROCESS_STARTER_NAME, SystemExecutors.PROCESS_STARTER_DESCRIPTION));
        // define executor permissions
        permissionDAO.addType(SecuredObjectType.ACTOR, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.GROUP, adminWithGroupExecutors);
        // define system permissions
        permissionDAO.addType(SecuredObjectType.SYSTEM, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATIONGROUP, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.RELATIONPAIR, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.BOTSTATION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.DEFINITION, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.PROCESS, adminWithGroupExecutors);
        permissionDAO.addType(SecuredObjectType.REPORT, adminWithGroupExecutors);
    }

    /**
     * Apply patches to initialized database.
     */
    private void applyPatches(UserTransaction transaction, int dbVersion) {
        log.info("Database version: " + dbVersion + ", code version: " + dbPatches.size());
        while (dbVersion < dbPatches.size()) {
            DBPatch patch = ApplicationContextFactory.createAutowiredBean(dbPatches.get(dbVersion));
            dbVersion++;
            log.info("Applying patch " + patch + " (" + dbVersion + ")");
            try {
                transaction.begin();
                patch.executeDDLBefore();
                patch.executeDML();
                patch.executeDDLAfter();
                constantDAO.setDatabaseVersion(dbVersion);
                transaction.commit();
                log.info("Patch " + patch.getClass().getName() + "(" + dbVersion + ") is applied to database successfully.");
            } catch (Throwable th) {
                log.error("Can't apply patch " + patch.getClass().getName() + "(" + dbVersion + ").", th);
                Utils.rollbackTransaction(transaction);
                break;
            }
        }
    }

    // this method is outside patches because relies on hibernate entities
    // may be add some method for this case in DBPatch?
    private void postProcessPatches(UserTransaction transaction) {
        try {
            // v44
            transaction.begin();
            if (permissionDAO.getPrivilegedExecutors(SecuredObjectType.REPORT).isEmpty()) {
                log.info("Adding " + SecuredObjectType.REPORT + " tokens message hash");
                String administratorName = SystemProperties.getAdministratorName();
                Actor admin = executorDAO.getActor(administratorName);
                String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
                Group adminGroup = executorDAO.getGroup(administratorsGroupName);
                List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
                permissionDAO.addType(SecuredObjectType.REPORT, adminWithGroupExecutors);
            }
            transaction.commit();
        } catch (Throwable th) {
            log.error("Can't apply post-processor of CreateReportsTables", th);
            Utils.rollbackTransaction(transaction);
        }
        try {
            // v54
            transaction.begin();
            List<Token> tokens = tokenDAO.findByMessageHashIsNullAndExecutionStatusIsActive();
            if (!tokens.isEmpty()) {
                log.info("Updating " + tokens.size() + " tokens message hash");
                for (Token token : tokens) {
                    ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                    BaseMessageNode messageNode = (BaseMessageNode) processDefinition.getNodeNotNull(token.getNodeId());
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, token.getProcess());
                    String messageHash = Utils.getReceiveMessageNodeHash(executionContext.getVariableProvider(), messageNode);
                    token.setMessageHash(messageHash);
                }
            }
            transaction.commit();
        } catch (Throwable th) {
            log.error("Can't apply post-processor of AddTokenMessageHashPatch", th);
            Utils.rollbackTransaction(transaction);
        }
    }
}
