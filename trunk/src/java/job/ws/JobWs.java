/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package job.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author dida
 */
@WebService(serviceName = "WSSubmitJob")
public class JobWs {

    /**
     * This is a sample web service operation
     */
    //Directories
    final String gridDir = "/biomed/user/l/louacheni";
    final String diracDir = "/home/dida/DIRAC/scripts";
    final String filesDir = "/home/dida/DIRAC";
    final String gridInput = gridDir + "/scripts";
    final String gridFiles = gridDir + "/files";
    final String gridTools = gridDir + "/tools";
    int nClone = 6;
    int j;
    String line = "";

    @WebMethod(operationName = "generateJDL")

    public ArrayList<String> submitQualityJob(@WebParam(name = "file") String file) {
        ArrayList<String> jobsid = new ArrayList<String>();

        // Generate JDL file for quality job
        for (int clone = 1; clone <= nClone; clone++) {
            generateJDL(file, clone);
            // Submit quality job with generated JDL file
            String[] cmd = {diracDir + "/dirac-wms-job-submit", filesDir + "/" + file + "/docking-" + clone + ".jdl"};
            String jobid = executeShellCommand(cmd);;
            jobsid.add(jobid);
        }
        return jobsid;
    }

    private ArrayList<String> generateJDL(String file, int clone) {
        ArrayList<String> files = new ArrayList<String>();
        //String lineFile = "";

        File fileDir = new File(filesDir + "/" + file);
        //create file if it doesn't exist
        if (!fileDir.exists()) {
            //create didrectory
            boolean create = fileDir.mkdir(); //or fileDir.mkdir();
            if (!create) {
                System.out.println("Error creating directory" + fileDir);
            }
            String path = fileDir.getAbsolutePath();
            System.out.println("The path of the file is " + path);
        }
        //open file name
        String fileName = fileDir + "/docking-" + j + ".jdl";
        try {
            //write to file        
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            //job name
            bw.write("JobName = \"docking-" + j + "-" + file + "\";");
            bw.newLine();
            //executable
            bw.write("Executable = \"" + filesDir + "/dockF" + j + ".sh\" ;");
            bw.newLine();
            //output
            bw.write("StdOutput = \"stdOut\";");
            bw.newLine();
            //error
            bw.write("StdError = \"stdErr\";");
            bw.newLine();
            //inputsand box
            bw.write("InputSandbox = {\"LFN:" + gridFiles + "/dpfFile.txt\","
                    + "\"" + filesDir + "/autodock.sh\","
                    + "\"" + filesDir + "/jobDG.sh\","
                    + "\"LFN:" + gridFiles + "/ligand.txt\","
                    + "\"LFN:" + gridFiles + "/1OKE.txt\","
                    + "\"LFN:" + gridTools + "/autodock.tar.gz\","
                    + "\"LFN:" + gridInput + "/autodock.sh\","
                    + "\"LFN:" + gridInput + "/dock.sh\","
                    + "\"LFN:" + gridDir + "/dockFile.tar.gz\"};");
            bw.newLine();
            //outputSandbox
            bw.write("OutputSandbox = {\"stdout\", \"stdErr\", \"dockFile.tar.bz2\", \"dockFile1.tar.bz2\";}");
            bw.newLine();
            //close file
            bw.close();
            System.out.println("Done");
            //}
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        files.add(fileName);
        return files;

    }

    private String executeShellCommand(String[] command) {
        ArrayList<String> lineCommand = new ArrayList<String>();
        String line = "";
        try {
            Runtime env = Runtime.getRuntime();
            Process process = env.exec(command);
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(isr);

            while ((line = input.readLine()) != null) {
                System.out.println(line);
                lineCommand.add(line);
            }
            for ( int i = 0; i <= lineCommand.size(); i++){
                return  lineCommand.get(i);
            }
            int exitVal = process.waitFor();
            System.out.println("Exited with error code " + exitVal);
            process.getInputStream().close();
            input.close();
            process.destroy();
        } catch (Exception e) {
            //e.printStackTrace();
            line = "Error: " + e.getMessage();
        }
        return line;
    }

    //get the status of the job
    @WebMethod(operationName = "getStatusJob")
    @SuppressWarnings("empty-statement")
    public String getStatusJob(@WebParam(name = "jobID") String jobID) {
        String jobStatOut = "";

        if (jobID.toLowerCase().startsWith("jobid")) {
            //extract the job id
            String idJob = jobID.substring(8, jobID.length());
            //execute command shell to get the status of the job
            String[] statCmd = {filesDir + "/jobStatus.sh", idJob};
            jobStatOut = executeShellCommand(statCmd);
            if (!jobStatOut.isEmpty()) {
                return jobStatOut.toLowerCase();
            } else {
                return "failed Job";
            }
        } else {
            return "failed Job";
        }

    }

}
