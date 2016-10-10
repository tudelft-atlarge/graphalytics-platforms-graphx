package nl.tudelft.granula.modeller;

import nl.tudelft.granula.archiver.GranulaExecutor;
import nl.tudelft.granula.modeller.entity.Execution;
import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.granula.util.json.JsonUtil;
import nl.tudelft.graphalytics.granula.GranulaPlugin;

import java.nio.file.Paths;

/**
 * Created by wing on 21-8-15.
 */
public class ModelTester {
    public static void main(String[] args) {
        String inputPath = "/home/wlngai/Workstation/Exec/Granula/debug/archiver/graphx/log";
        String driverLogPath = "/home/wlngai/Workstation/Exec/Granula/debug/archiver/graphx/log/execution/execution-log.js";
        String outputPath = "/home/wlngai/Workstation/Exec/Granula/debug/archiver/graphx/arc";


        Execution execution = (Execution) JsonUtil.fromJson(FileUtil.readFile(Paths.get(driverLogPath)), Execution.class);
        execution.setLogPath(inputPath);
        execution.setEndTime(System.currentTimeMillis());
        execution.setArcPath(outputPath);
        JobModel jobModel = new JobModel(GranulaPlugin.getPlatformModel(execution.getPlatform()));

        GranulaExecutor granulaExecutor = new GranulaExecutor();
        granulaExecutor.setExecution(execution);
        granulaExecutor.buildJobArchive(jobModel);

    }
}