package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Entity.*;
import com.ahana.botmonitoring.Repository.*;
import com.ahana.botmonitoring.Mapper.ExecutionTimeMapper;

import com.ahana.botmonitoring.generated.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LogService {

    private final LogRepository logRepository;
    private final CompletedTaskRepository completedTaskRepository;
    private final ExecutionTimeRepository executionTimeRepository;

    private final LogPythonRepository logPythonRepository;

    private final PythonExecutionTimeRepository pythonExecutionTimeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MongoTemplate mongoTemplate;
    private final ExecutionTimeMapper executionTimeMapper;

    public LogService(LogRepository logRepository, CompletedTaskRepository completedTaskRepository,
            MongoTemplate mongoTemplate, ExecutionTimeRepository executionTimeRepository,
            LogPythonRepository logPythonRepository, PythonExecutionTimeRepository pythonExecutionTimeRepository,
            ExecutionTimeMapper executionTimeMapper) {
        this.logRepository = logRepository;
        this.completedTaskRepository = completedTaskRepository;
        this.mongoTemplate = mongoTemplate;
        this.executionTimeRepository = executionTimeRepository;
        this.logPythonRepository = logPythonRepository;
        this.pythonExecutionTimeRepository = pythonExecutionTimeRepository;
        this.executionTimeMapper = executionTimeMapper;
    }

    // public String processLogFile(MultipartFile file) {
    // Map<String, List<LogEntry>> logsByJobId = new HashMap<>();
    // List<LogEntry> allValidLogs = new ArrayList<>();
    // List<CompletedTask> completedTasks = new ArrayList<>();
    //
    // try (BufferedReader br = new BufferedReader(new
    // InputStreamReader(file.getInputStream()))) {
    //
    // String line;
    // while ((line = br.readLine()) != null) {
    // int jsonStart = line.indexOf('{');
    // if (jsonStart == -1) continue;
    //
    // String jsonPart = line.substring(jsonStart);
    // LogEntry entry = objectMapper.readValue(jsonPart, LogEntry.class);
    //
    // // ✅ Group all logs by jobId (even Trace/User ones)
    // String jobId = entry.getJobId();
    // if (jobId == null) continue;
    //
    // logsByJobId.computeIfAbsent(jobId, k -> new ArrayList<>()).add(entry);
    // }
    //
    // // ✅ Now filter and extract completed tasks
    // for (Map.Entry<String, List<LogEntry>> entry : logsByJobId.entrySet()) {
    // String jobId = entry.getKey();
    // List<LogEntry> logs = entry.getValue(); // Unfiltered logs
    //
    // // ✅ STEP 1: From unfiltered logs, detect execution ended and extract
    // execution time
    // for (LogEntry log : logs) {
    // String message = log.getMessage();
    // if (message == null) continue;
    //
    // if (message.endsWith("execution ended")) {
    // CompletedTask task = new CompletedTask();
    // task.setJobId(jobId);
    // task.setTotalExecutionTime(log.getTotalExecutionTime());
    // completedTasks.add(task);
    // }
    // }
    //
    // // ✅ STEP 2: Now apply filtering and save valid logs only
    // List<LogEntry> filteredLogs = logs.stream()
    // .filter(log -> !("Trace".equalsIgnoreCase(log.getLevel()) &&
    // "User".equalsIgnoreCase(log.getLogType())))
    // .collect(Collectors.toList());
    //
    // allValidLogs.addAll(filteredLogs);
    // }
    //
    // logRepository.saveAll(allValidLogs);
    // completedTaskRepository.saveAll(completedTasks);
    //
    // return "Log processed successfully. Inserted logs: " + allValidLogs.size() +
    // ", Completed Tasks: " + completedTasks.size();
    //
    // } catch (Exception e) {
    // return "Error processing log file: " + e.getMessage();
    // }
    // }

    // public String processLogFile(MultipartFile file) {
    // Map<String, List<Log>> logsByJobId = new HashMap<>();
    // List<Log> allValidLogs = new ArrayList<>();
    // List<ExecutionTime> executionTimes = new ArrayList<>();
    //
    // try (BufferedReader br = new BufferedReader(new
    // InputStreamReader(file.getInputStream()))) {
    //
    // String line;
    // while ((line = br.readLine()) != null) {
    // int jsonStart = line.indexOf('{');
    // if (jsonStart == -1) continue;
    //
    // String jsonPart = line.substring(jsonStart);
    // Log entry = objectMapper.readValue(jsonPart, Log.class);
    //
    // String jobId = entry.getJobId();
    // if (jobId == null) continue;
    //
    // logsByJobId.computeIfAbsent(jobId, k -> new ArrayList<>()).add(entry);
    // }
    //
    // // ✅ Process logs per jobId
    // for (Map.Entry<String, List<Log>> entry : logsByJobId.entrySet()) {
    // String jobId = entry.getKey();
    // List<Log> logs = entry.getValue();
    //
    // for (Log log : logs) {
    // String message = log.getMessage();
    // if (message == null) continue;
    //
    // if (message.endsWith("execution ended")) {
    // try {
    // LocalDateTime parsedDateTime = null;
    // LocalDate parsedDate = null;
    //
    // // ✅ Parse timestamp (always String in your model)
    // if (log.getTimeStamp() != null) {
    // try {
    // parsedDateTime = LocalDateTime.parse(
    // log.getTimeStamp(),
    // DateTimeFormatter.ISO_DATE_TIME
    // );
    // parsedDate = parsedDateTime.toLocalDate();
    // } catch (Exception dtEx) {
    // System.err.println("⚠️ Failed to parse timestamp: " + log.getTimeStamp());
    // }
    // }
    //
    // // ✅ Convert for ExecutionTime fields
    // String logDate = (parsedDate != null) ? parsedDate.toString() : null;
    // String startTime = (parsedDateTime != null) ? parsedDateTime.toString() :
    // null;
    // String endTime = (parsedDateTime != null) ? parsedDateTime.toString() : null;
    // int totalSeconds = 0;
    // if (log.getTotalExecutionTime() != null &&
    // log.getTotalExecutionTime().contains(":")) {
    // try {
    // String[] parts = log.getTotalExecutionTime().split(":");
    // int hours = Integer.parseInt(parts[0]);
    // int minutes = Integer.parseInt(parts[1]);
    // int seconds = Integer.parseInt(parts[2]);
    // totalSeconds = hours * 3600 + minutes * 60 + seconds;
    // } catch (Exception ex) {
    // System.err.println("⚠️ Failed to parse totalExecutionTime: " +
    // log.getTotalExecutionTime());
    // }
    // }
    // ExecutionTime exec = new ExecutionTime(
    // logDate,
    // startTime,
    // endTime,
    // log.getProcessName(),
    // log.getBotName(),
    // String.valueOf(totalSeconds), // ✅ renamed variable
    // parsedDate,
    // parsedDateTime,
    // log.getLevel(),
    // parsedDateTime, // endDateTime
    // new ArrayList<>(), // errorMessages
    // log.getMachineName(),
    // new Date(),
    // jobId
    // );
    //
    // executionTimes.add(exec);
    //
    // } catch (Exception pe) {
    // System.err.println("⚠️ Could not parse timestamp for jobId " + jobId + ": " +
    // pe.getMessage());
    // }
    // }
    // }
    //
    // // ✅ Filter logs before saving
    // List<Log> filteredLogs = logs.stream()
    // .filter(l -> !("Trace".equalsIgnoreCase(l.getLevel()) &&
    // "User".equalsIgnoreCase(l.getLogType())))
    // .collect(Collectors.toList());
    //
    // allValidLogs.addAll(filteredLogs);
    // }
    //
    // // ✅ Save to Mongo
    // logRepository.saveAll(allValidLogs);
    // executionTimeRepository.saveAll(executionTimes);
    //
    // return "Log processed successfully. Inserted logs: " + allValidLogs.size() +
    // ", ExecutionTime entries: " + executionTimes.size();
    //
    // } catch (Exception e) {
    // return "Error processing log file: " + e.getMessage();
    // }
    // }

    public String processLogFile(MultipartFile file) throws IOException {
        List<String> list = new ArrayList<>();
        List<String> finallist = new ArrayList<>();

        String content = new String(file.getBytes());
        Scanner scanner = new Scanner(content);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            list.add(line);
        }
        scanner.close();

        // System.out.println("Total no of logs found " + list.size());

        // for (String logLine : list) {
        // finallist.add(logLine.substring(logLine.lastIndexOf("{")));
        // }
        for (String logLine : list) {
            int startIndex = logLine.lastIndexOf("{");
            if (startIndex != -1) { // ✅ only process if JSON part exists
                finallist.add(logLine.substring(startIndex));
            } else {
                // System.out.println("Skipping line (no JSON found): " + logLine);
            }
        }

        // System.out.println("Total no of json objects found " + finallist.size());

        // Save logs
        for (String json : finallist) {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            Log log1 = new Log();
            log1.setMessage(jsonObject.get("message").getAsString());
            log1.setLevel(jsonObject.get("level").getAsString());
            log1.setLogType(jsonObject.get("logType").getAsString());

            String result = jsonObject.get("timeStamp").getAsString().split("\\.")[0];
            LocalDateTime dateTime1 = LocalDateTime.parse(result);
            log1.setTimeStamp(dateTime1);

            log1.setFingerprint(jsonObject.get("fingerprint").getAsString());
            log1.setWindowsIdentity(jsonObject.get("windowsIdentity").getAsString());
            log1.setMachineName(jsonObject.get("machineName").getAsString());
            log1.setProcessName(jsonObject.get("processName").getAsString());
            log1.setProcessVersion(jsonObject.get("processVersion").getAsString());
            log1.setJobId(jsonObject.get("jobId").getAsString());
            log1.setRobotName(jsonObject.get("robotName").getAsString());
            log1.setMachineId(jsonObject.get("machineId").getAsString());

            if (!log1.getLogType().contains("User")) {
                log1.setFileName(jsonObject.get("fileName").getAsString());
            }
            if (log1.getMessage().contains("execution started")) {
                log1.setInitiatedBy(jsonObject.get("initiatedBy").getAsString());
            }
            if (log1.getMessage().contains("execution ended")) {
                log1.setTotalExecutionTimeInSeconds(jsonObject.get("totalExecutionTimeInSeconds").getAsString());
                log1.setTotalExecutionTime(jsonObject.get("totalExecutionTime").getAsString());
            }

            logRepository.save(log1);
        }

        List<Log> log = logRepository.findAll();
        // System.out.println("Logs in DB: " + log.size());

        calculateExecutionTimes(log);

        return "saved the log file and calculated the utilisation";
    }

    private void calculateExecutionTimes(List<Log> log) {
        int noOfStartedNew = 0;
        int lastStartedIndex = 0;

        for (int j = 0; j < log.size(); j++) {
            if (log.get(j).getMessage().contains("execution started")) {
                noOfStartedNew++;
                lastStartedIndex = j;
            }
        }
        // System.out.println("noOfStartedNew " + noOfStartedNew);

        int noOfStarted = 0;

        for (int k = 0; k < log.size(); k++) {
            if (log.get(k).getMessage().contains("execution started")) {
                ExecutionTime processDetails = new ExecutionTime();
                noOfStarted++;

                LocalDateTime logDate = log.get(k).getTimeStamp();
                String result = logDate.toString().substring(0, logDate.toString().lastIndexOf("T"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateTime = LocalDate.parse(result, formatter);

                processDetails.setLogDateinLocalDate(dateTime);
                Date out = Date.from(logDate.atZone(ZoneId.systemDefault()).toInstant());
                processDetails.setCreatedDate(out);

                LocalDateTime firstStartedTimeStamp = log.get(k).getTimeStamp();
                processDetails.setStartTimeinLocalDateTime(firstStartedTimeStamp);
                processDetails.setBotId(log.get(k).getBotId());
                processDetails.setBotName(log.get(k).getBotName());
                processDetails.setProcessName(log.get(k).getProcessName());
                processDetails.setProcessStatus("Started");
                processDetails.setJobId(log.get(k).getJobId());

                if (k == lastStartedIndex) {
                    handleLastStarted(log, k, processDetails, firstStartedTimeStamp);
                } else {
                    handleIntermediateStarted(log, k, lastStartedIndex, processDetails, firstStartedTimeStamp);
                }
            }
        }

        // System.out.println(noOfStarted + " total no of started");
    }

    private void handleLastStarted(List<Log> log, int k, ExecutionTime processDetails,
            LocalDateTime firstStartedTimeStamp) {
        // System.out.println("INSIDE THE LAST STARTED -- CALCULATOR last started at " +
        // k);

        for (int x = k + 1; x < log.size(); x++) {
            if (log.get(x).getMessage().contains("execution ended")) {
                // System.out.println("Scenario: Started-Ended");
                LocalDateTime processEndedTimeStamp = log.get(x).getTimeStamp();
                processDetails.setEndTimeinLocalDateTime(processEndedTimeStamp);
                Duration duration = Duration.between(firstStartedTimeStamp, processEndedTimeStamp);
                processDetails.setTimeDifference(Long.toString(duration.toSeconds()));
                processDetails.setProcessStatus("Successfully Ended");

                List<String> errorMessages = new ArrayList<>();
                for (int m = k + 1; m < x - 1; m++) {
                    if (log.get(m).getLevel().contains("Error")) {
                        // System.out.println("Scenario: Started-Error-Ended");
                        errorMessages.add(log.get(m).getMessage());
                        processDetails.setProcessStatus("Successfully Ended");
                    }
                }
                processDetails.setErrorMessage(errorMessages);
            }
        }
        executionTimeRepository.save(processDetails);
    }

    private void handleIntermediateStarted(List<Log> log, int k, int lastStartedIndex,
            ExecutionTime processDetails, LocalDateTime firstStartedTimeStamp) {
        // System.out.println("First Started at " + k);

        for (int m = k + 1; m <= lastStartedIndex; m++) {
            if (log.get(m).getMessage().contains("execution started")) {
                if (m == k + 1) {
                    // System.out.println("Scenario: Started-Started");
                    processDetails.setProcessStatus("Failed To End Started-Started");
                }
                if (log.get(m - 1).getMessage().contains("execution ended")) {
                    // System.out.println("Scenario: Started-Ended");
                    LocalDateTime processEndedTimeStamp = log.get(m - 1).getTimeStamp();
                    processDetails.setEndTimeinLocalDateTime(processEndedTimeStamp);
                    Duration duration = Duration.between(firstStartedTimeStamp, processEndedTimeStamp);
                    processDetails.setTimeDifference(Long.toString(duration.toSeconds()));
                    processDetails.setProcessStatus("Successfully Ended");

                    List<String> errorMessages = new ArrayList<>();
                    for (int n = k + 1; n < m - 1; n++) {
                        if (log.get(n).getLevel().contains("Error")) {
                            // System.out.println("Scenario: Started-Error-Ended");
                            errorMessages.add(log.get(n).getMessage());
                            processDetails.setProcessStatus("Successfully Ended");
                        }
                    }
                    processDetails.setErrorMessage(errorMessages);
                } else {
                    // System.out.println("Scenario: Started-Info-Started");
                    LocalDateTime processEndedTimeStamp = log.get(m - 1).getTimeStamp();
                    processDetails.setEndTimeinLocalDateTime(processEndedTimeStamp);
                    Duration duration = Duration.between(firstStartedTimeStamp, processEndedTimeStamp);
                    processDetails.setTimeDifference(Long.toString(duration.toSeconds()));
                    processDetails.setProcessStatus("Failed to End");
                }
                executionTimeRepository.save(processDetails);
                break;
            }
        }
    }

    public List<AllBotDetailUtilisationResponse> getAllBotDetailUtilisation(RequestAllBotUtilisation request) {
        List<AllBotDetailUtilisationResponse> detailUtilisation = new ArrayList<>();
        List<String> botNames = request.getBotName();

        for (String botName : botNames) {
            Query botQuery = new Query();
            botQuery.addCriteria(Criteria.where("botName").is(botName));
            List<ExecutionTime> executionTimes = mongoTemplate.find(botQuery, ExecutionTime.class);

            Set<String> uniqueProcesses = executionTimes.stream()
                    .map(ExecutionTime::getProcessName)
                    .collect(Collectors.toSet());

            for (String processName : uniqueProcesses) {
                // Query execution times for this process within the time range
                Query processQuery = new Query();
                processQuery.addCriteria(Criteria.where("processName").is(processName));
                processQuery.addCriteria(Criteria.where("createdDate")
                        .gte(request.getStartTime())
                        .lt(request.getEndTime()));

                List<ExecutionTime> processExecutionTimes = mongoTemplate.find(processQuery, ExecutionTime.class);

                long totalProcessUtilisation = processExecutionTimes.stream()
                        .mapToLong(et -> {
                            try {
                                return Long.parseLong(et.getTimeDifference());
                            } catch (Exception e) {
                                return 0L;
                            }
                        })
                        .sum();

                List<Long> dataList = new ArrayList<>();
                dataList.add(totalProcessUtilisation);

                // Add zeros for previous bots
                int botIndex = botNames.indexOf(botName);
                for (int i = 0; i < botIndex; i++) {
                    dataList.add(0L);
                }

                AllBotDetailUtilisationResponse response = new AllBotDetailUtilisationResponse();
                response.setBotName(botName);
                response.setProcessName(processName);
                response.setData(dataList);

                detailUtilisation.add(response);
            }
        }
        return detailUtilisation;
    }

    public JSONObject calculateAllBotUtilisation(RequestAllBotUtilisation request) {
        JSONObject obj = new JSONObject();

        for (String botName : request.getBotName()) {
            // Build query for this bot
            Query query = new Query();
            query.addCriteria(Criteria.where("botName").is(botName));
            query.addCriteria(Criteria.where("createdDate")
                    .gte(request.getStartTime())
                    .lt(request.getEndTime()));

            // Fetch execution time records
            List<ExecutionTime> executionTimes = mongoTemplate.find(query, ExecutionTime.class);

            // Calculate total utilisation
            long totalBotUtilisation = executionTimes.stream()
                    .mapToLong(e -> Long.parseLong(e.getTimeDifference()))
                    .sum();

            // obj.addProperty(botName, totalBotUtilisation);
            obj.put(botName, totalBotUtilisation);
        }

        return obj;
    }

    // public JSONObject getBotUtilisationNew(RequestBotUtilisation request) {
    // List<ExecutionTime> ListProcess = new ArrayList<>();
    // JSONObject obj = new JSONObject();
    //
    //
    //
    // long timeDiff = Math.abs(request.getStartTime().getTime() -
    // request.getEndTime().getTime());
    // long daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
    // daysDiff++;
    //
    // Date startDate = request.getStartTime();
    //
    // for (int i = 1; i < daysDiff + 1L; i++) {
    // String days = "day" + i;
    // //System.out.println(days);
    //
    // Date incDate = new Date(startDate.getTime() + 86400000L);
    // //System.out.println(incDate);
    //
    // Query query = new Query();
    // query.addCriteria(Criteria.where("botName").is(request.getBotName()));
    // query.addCriteria(Criteria.where("createdDate").gte(startDate).lt(incDate));
    //
    // ListProcess.addAll(this.mongoTemplate.find(query, ExecutionTime.class));
    //
    // long singleDayBotUtilisation = 0L;
    // for (int j = 0; j < ListProcess.size(); j++) {
    // //System.out.println(ListProcess.get(j));
    // singleDayBotUtilisation +=
    // Integer.parseInt(ListProcess.get(j).getTimeDifference());
    // }
    //
    // obj.put(days, Long.valueOf(singleDayBotUtilisation));
    // startDate = incDate;
    // ListProcess.clear();
    // }
    //
    // return obj;
    // }

    public JSONObject getBotUtilisationNew(RequestBotUtilisation request) {
        List<ExecutionTime> listProcess = new ArrayList<>();
        JSONObject obj = new JSONObject();

        long daysDiff = ChronoUnit.DAYS.between(request.getStartTime(), request.getEndTime()) + 1;

        LocalDate startDate = request.getStartTime();

        for (int i = 1; i <= daysDiff; i++) {
            String days = "day" + i;
            // System.out.println(days);

            LocalDate nextDate = startDate.plusDays(1);
            // System.out.println(nextDate);

            // Convert LocalDate -> Date (start of day / next day)
            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Query query = new Query();
            query.addCriteria(Criteria.where("botName").is(request.getBotName()));
            query.addCriteria(Criteria.where("createdDate").gte(start).lt(end));

            listProcess.addAll(this.mongoTemplate.find(query, ExecutionTime.class));

            long singleDayBotUtilisation = 0L;
            for (ExecutionTime et : listProcess) {
                // System.out.println(et);
                singleDayBotUtilisation += Integer.parseInt(et.getTimeDifference());
            }

            obj.put(days, singleDayBotUtilisation);
            startDate = nextDate;
            listProcess.clear();
        }

        return obj;
    }

    // public List<ExecutionTime> filterLogsNew(RequestUtilisationNew request) {
    // List<ExecutionTime> resultList = new ArrayList<>();
    //
    // switch (request.getScenario()) {
    // case "scenario1":
    // for (String botName : request.getBotName()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("botName").is(botName));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario2":
    // for (String processName : request.getProcessName()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("processName").is(processName));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario3":
    // for (String status : request.getStatus()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("processStatus").is(status));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario4":
    // for (String botName : request.getBotName()) {
    // for (String processName : request.getProcessName()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("botName").is(botName));
    // query.addCriteria(Criteria.where("processName").is(processName));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // case "scenario5":
    // for (String botName : request.getBotName()) {
    // for (String status : request.getStatus()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("botName").is(botName));
    // query.addCriteria(Criteria.where("processStatus").is(status));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // case "scenario6":
    // for (String botName : request.getBotName()) {
    // for (String processName : request.getProcessName()) {
    // for (String status : request.getStatus()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("botName").is(botName));
    // query.addCriteria(Criteria.where("processName").is(processName));
    // query.addCriteria(Criteria.where("processStatus").is(status));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // }
    // }
    // break;
    //
    // case "scenario7":
    // for (String processName : request.getProcessName()) {
    // for (String status : request.getStatus()) {
    // Query query = new Query();
    // query.addCriteria(Criteria.where("processName").is(processName));
    // query.addCriteria(Criteria.where("processStatus").is(status));
    // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // case "scenario8":
    // Query query8 = new Query();
    // query8.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList = mongoTemplate.find(query8, ExecutionTime.class);
    // break;
    //
    // case "scenario9":
    // for (String botName : request.getBotName()) {
    // Query query9 = new Query();
    // query9.addCriteria(Criteria.where("botName").is(botName));
    // query9.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query9, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario10":
    // for (String botName : request.getBotName()) {
    // for (String processName : request.getProcessName()) {
    // Query query10 = new Query();
    // query10.addCriteria(Criteria.where("botName").is(botName));
    // query10.addCriteria(Criteria.where("processName").is(processName));
    // query10.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query10, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // case "scenario11":
    // for (String botName : request.getBotName()) {
    // for (String processName : request.getProcessName()) {
    // for (String status : request.getStatus()) {
    // Query query11 = new Query();
    // query11.addCriteria(Criteria.where("botName").is(botName));
    // query11.addCriteria(Criteria.where("processName").is(processName));
    // query11.addCriteria(Criteria.where("processStatus").is(status));
    // query11.addCriteria(Criteria.where("logDateinLocalDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query11, ExecutionTime.class));
    // }
    // }
    // }
    // break;
    //
    // case "scenario12":
    // for (String processName : request.getProcessName()) {
    // Query query12 = new Query();
    // query12.addCriteria(Criteria.where("processName").is(processName));
    // query12.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query12, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario13":
    // for (String processName : request.getProcessName()) {
    // for (String status : request.getStatus()) {
    // Query query13 = new Query();
    // query13.addCriteria(Criteria.where("processName").is(processName));
    // query13.addCriteria(Criteria.where("processStatus").is(status));
    // query13.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query13, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // case "scenario14":
    // for (String status : request.getStatus()) {
    // Query query14 = new Query();
    // query14.addCriteria(Criteria.where("processStatus").is(status));
    // query14.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query14, ExecutionTime.class));
    // }
    // break;
    //
    // case "scenario15":
    // for (String botName : request.getBotName()) {
    // for (String status : request.getStatus()) {
    // Query query15 = new Query();
    // query15.addCriteria(Criteria.where("botName").is(botName));
    // query15.addCriteria(Criteria.where("processStatus").is(status));
    // query15.addCriteria(Criteria.where("createdDate")
    // .gte(request.getStartTime())
    // .lt(request.getEndTime()));
    // resultList.addAll(mongoTemplate.find(query15, ExecutionTime.class));
    // }
    // }
    // break;
    //
    // default:
    // return Collections.emptyList();
    // }
    //
    // return resultList;
    // }

    public List<ExecutionTimeDTO> filterLogsNew(RequestUtilisationNew request) {
        String scenario = request.getScenario();
        List<ExecutionTime> resultList = new ArrayList<>();
        Date start = request.getStartTime() != null
                ? Date.from(request.getStartTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                : null;
        Date end = request.getEndTime() != null
                ? Date.from(request.getEndTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                : null;
        Calendar cal = Calendar.getInstance();
        if (end != null) {
            cal.setTime(end);
            cal.add(Calendar.DATE, 1);
            end = cal.getTime();
        }

        switch (scenario) {
            case "scenario1":
                for (String botName : request.getBotName()) {
                    Query query = new Query(Criteria.where("botName").is(botName));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            case "scenario2":
                for (String processName : request.getProcessName()) {
                    Query query = new Query(Criteria.where("processName").is(processName));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            case "scenario3":
                for (String status : request.getStatus()) {
                    Query query = new Query(Criteria.where("processStatus").is(status));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            case "scenario4":
                for (String botName : request.getBotName()) {
                    for (String processName : request.getProcessName()) {
                        Query query = new Query();
                        query.addCriteria(Criteria.where("botName").is(botName));
                        query.addCriteria(Criteria.where("processName").is(processName));
                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            case "scenario5":
                for (String botName : request.getBotName()) {
                    for (String status : request.getStatus()) {
                        Query query = new Query();
                        query.addCriteria(Criteria.where("botName").is(botName));
                        query.addCriteria(Criteria.where("processStatus").is(status));
                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            case "scenario6":
                for (String botName : request.getBotName()) {
                    for (String processName : request.getProcessName()) {
                        for (String status : request.getStatus()) {
                            Query query = new Query();
                            query.addCriteria(Criteria.where("botName").is(botName));
                            query.addCriteria(Criteria.where("processName").is(processName));
                            query.addCriteria(Criteria.where("processStatus").is(status));
                            resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                        }
                    }
                }
                break;

            case "scenario7":
                for (String processName : request.getProcessName()) {
                    for (String status : request.getStatus()) {
                        Query query = new Query();
                        query.addCriteria(Criteria.where("processName").is(processName));
                        query.addCriteria(Criteria.where("processStatus").is(status));
                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            case "scenario8":
                resultList = mongoTemplate.find(new Query(
                        Criteria.where("createdDate")
                                .gte(request.getStartTime())
                                .lt(request.getEndTime())),
                        ExecutionTime.class);
                break;

            case "scenario9":
                for (String botName : request.getBotName()) {
                    Query query = new Query();
                    query.addCriteria(Criteria.where("botName").is(botName));
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            // case "scenario10":
            // for (String botName : request.getBotName()) {
            // for (String processName : request.getProcessName()) {
            // Query query = new Query();
            // query.addCriteria(Criteria.where("botName").is(botName));
            // query.addCriteria(Criteria.where("processName").is(processName));
            // query.addCriteria(Criteria.where("createdDate")
            // .gte(request.getStartTime())
            // .lt(request.getEndTime()));
            // resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
            // }
            // }
            // break;
            case "scenario10":
                for (String botName : request.getBotName()) {
                    for (String processName : request.getProcessName()) {

                        Query query = new Query();
                        query.addCriteria(Criteria.where("botName").is(botName));
                        query.addCriteria(Criteria.where("processName").is(processName));
                        query.addCriteria(Criteria.where("createdDate").gte(start).lt(end));

                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            case "scenario11":
                for (String botName : request.getBotName()) {
                    for (String processName : request.getProcessName()) {
                        for (String status : request.getStatus()) {
                            Query query = new Query();
                            query.addCriteria(Criteria.where("botName").is(botName));
                            query.addCriteria(Criteria.where("processName").is(processName));
                            query.addCriteria(Criteria.where("processStatus").is(status));
                            Date starta = request.getStartTime() != null
                                    ? Date.from(request.getStartTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                                    : null;
                            Date enda = request.getEndTime() != null
                                    ? Date.from(request.getEndTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                                    : null;

                            Calendar c = Calendar.getInstance();
                            c.setTime(enda);
                            c.add(Calendar.DATE, 1); // add 1 day
                            Date nextDay = c.getTime();

                            query.addCriteria(Criteria.where("createdDate")
                                    .gte(starta)
                                    .lt(nextDay));
                            resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                        }
                    }
                }
                break;

            case "scenario12":
                for (String processName : request.getProcessName()) {
                    Query query = new Query();
                    query.addCriteria(Criteria.where("processName").is(processName));
                    Date startTime = request.getStartTime() != null
                            ? Date.from(request.getStartTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                            : null;
                    Date endTime = request.getEndTime() != null
                            ? Date.from(request.getEndTime().atStartOfDay(ZoneId.systemDefault()).toInstant())
                            : null;
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(startTime)
                            .lt(endTime));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            case "scenario13":
                for (String processName : request.getProcessName()) {
                    for (String status : request.getStatus()) {
                        Query query = new Query();
                        query.addCriteria(Criteria.where("processName").is(processName));
                        query.addCriteria(Criteria.where("processStatus").is(status));
                        query.addCriteria(Criteria.where("createdDate")
                                .gte(request.getStartTime())
                                .lt(request.getEndTime()));
                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            case "scenario14":
                for (String status : request.getStatus()) {
                    Query query = new Query();
                    query.addCriteria(Criteria.where("processStatus").is(status));
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                    resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                }
                break;

            case "scenario15":
                for (String botName : request.getBotName()) {
                    for (String status : request.getStatus()) {
                        Query query = new Query();
                        query.addCriteria(Criteria.where("botName").is(botName));
                        query.addCriteria(Criteria.where("processStatus").is(status));
                        query.addCriteria(Criteria.where("createdDate")
                                .gte(request.getStartTime())
                                .lt(request.getEndTime()));
                        resultList.addAll(mongoTemplate.find(query, ExecutionTime.class));
                    }
                }
                break;

            default:
                break;
        }

        return executionTimeMapper.toDTOList(resultList);
    }

    public List<ExecutionTimeDTO> filterLogs(RequestUtilsation request) {
        Query query = new Query();

        switch (request.getScenario()) {
            case "scenario1":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                break;

            case "scenario2":
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                break;

            case "scenario3":
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                break;

            case "scenario4":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                break;

            case "scenario5":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                break;

            case "scenario6":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                break;

            case "scenario7":
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                break;

            case "scenario8":
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario9":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario10":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario11":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario12":
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario13":
                if (request.getProcessName() != null && !request.getProcessName().isEmpty()) {
                    query.addCriteria(Criteria.where("processName").in(request.getProcessName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario14":
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            case "scenario15":
                if (request.getBotName() != null && !request.getBotName().isEmpty()) {
                    query.addCriteria(Criteria.where("botName").in(request.getBotName()));
                }
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                    query.addCriteria(Criteria.where("processStatus").in(request.getStatus()));
                }
                if (request.getStartTime() != null && request.getEndTime() != null) {
                    query.addCriteria(Criteria.where("createdDate")
                            .gte(request.getStartTime())
                            .lt(request.getEndTime()));
                }
                break;

            default:
                return Collections.emptyList();
        }

        List<ExecutionTime> results = mongoTemplate.find(query, ExecutionTime.class);
        log.debug("Scenario: {} -> {} records found", request.getScenario(), results.size());
        return executionTimeMapper.toDTOList(results);
    }

    public List<String> getUniqueBotNames() {
        List<String> categoryList = new ArrayList<>();

        // Get collection
        MongoCollection<?> mongoCollection = mongoTemplate.getCollection("executionTime");

        // Fetch distinct botName values
        DistinctIterable<String> distinctIterable = mongoCollection.distinct("botName", String.class);
        MongoCursor<String> cursor = distinctIterable.iterator();

        while (cursor.hasNext()) {
            String category = cursor.next();
            categoryList.add(category);
        }

        // Debug log (optional)
        for (String name : categoryList) {
            // System.out.println(name);
        }

        return categoryList;
    }

    private static final List<DateTimeFormatter> TIMESTAMP_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));

    public String processPythonLogFile(MultipartFile file) throws IOException {
        List<LogPython> logsToSave = new ArrayList<>();
        String content = new String(file.getBytes());
        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            try {
                LogPython log = parseLogLine(line);
                if (log != null) {
                    logsToSave.add(log);
                }
            } catch (Exception e) {
                log.error("Failed to parse line: {}", line, e);
            }
        }

        // Save all logs to database
        if (!logsToSave.isEmpty()) {
            logPythonRepository.saveAll(logsToSave);
            // System.out.println("Saved " + logsToSave.size() + " logs to database");
        }

        // Calculate execution times using UiPath pattern
        calculateExecutionTimesUiPathPattern(logsToSave);

        return String.format("Processed %d log entries from Python RPA script", logsToSave.size());
    }

    private LogPython parseLogLine(String line) {
        try {
            // Try to parse as JSON first (structured logging)
            if (line.trim().startsWith("{")) {
                return parseJsonLog(line);
            }

            // For non-JSON lines, check if they contain useful information
            return parseNonJsonLog(line);

        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            return createBasicLogEntry(line);
        }
    }

    private LogPython parseJsonLog(String line) {
        try {
            JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();
            LogPython log = new LogPython();

            // Extract timestamp - handle multiple possible field names
            LocalDateTime timestamp = parseTimestampFromJson(jsonObject);
            log.setTimestamp(timestamp);

            // Extract level - handle multiple possible field names
            String level = extractLevelFromJson(jsonObject);
            log.setLevel(level);

            // Extract message
            String message = extractMessageFromJson(jsonObject);
            log.setMessage(message);

            // Extract process name from JSON data
            String processName = extractProcessNameFromJson(jsonObject);
            log.setProcessName(processName);

            // Extract all additional fields
            Map<String, Object> additionalFields = extractAllAdditionalFields(jsonObject);
            log.setAdditionalFields(additionalFields);

            return log;

        } catch (Exception e) {
            System.err.println("Error parsing JSON log: " + line);
            e.printStackTrace();
            return createBasicLogEntry(line);
        }
    }

    private LogPython parseNonJsonLog(String line) {
        // For lines that don't start with {, try to extract information
        LogPython log = new LogPython();

        // Try to extract level from the line
        String level = extractLevelFromText(line);
        log.setLevel(level);

        log.setMessage(line);

        // Extract process name from the log content
        String processName = extractProcessNameFromText(line);
        log.setProcessName(processName);

        // For non-JSON logs, we need to extract timestamp from the original JSON format
        // This handles the case where JSON parsing failed but we have the raw JSON in
        // message
        if (line.contains("asctime") && line.contains("levelname")) {
            try {
                // Try to extract timestamp from the JSON-like string
                Pattern timestampPattern = Pattern
                        .compile("\"asctime\": \"(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})\"");
                var matcher = timestampPattern.matcher(line);
                if (matcher.find()) {
                    String timestampStr = matcher.group(1);
                    log.setTimestamp(parseTimestamp(timestampStr));
                } else {
                    log.setTimestamp(LocalDateTime.now());
                }
            } catch (Exception e) {
                log.setTimestamp(LocalDateTime.now());
            }
        } else {
            log.setTimestamp(LocalDateTime.now());
        }

        // Try to extract additional information
        Map<String, Object> additionalFields = new HashMap<>();
        if (line.contains("invoice")) {
            // Try to extract invoice ID
            Pattern invoicePattern = Pattern.compile("C24077\\d+");
            var matcher = invoicePattern.matcher(line);
            if (matcher.find()) {
                additionalFields.put("invoice_id", matcher.group());
            }
        }
        log.setAdditionalFields(additionalFields);

        return log;
    }

    private LocalDateTime parseTimestampFromJson(JsonObject jsonObject) {
        String[] timestampFields = { "asctime", "timestamp", "created", "time" };

        for (String field : timestampFields) {
            if (jsonObject.has(field) && !jsonObject.get(field).isJsonNull()) {
                try {
                    String timestampStr = jsonObject.get(field).getAsString();
                    return parseTimestamp(timestampStr);
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
        }

        return LocalDateTime.now();
    }

    private String extractLevelFromJson(JsonObject jsonObject) {
        String[] levelFields = { "levelname", "level", "log_level" };

        for (String field : levelFields) {
            if (jsonObject.has(field) && !jsonObject.get(field).isJsonNull()) {
                return jsonObject.get(field).getAsString();
            }
        }

        return "UNKNOWN";
    }

    private String extractMessageFromJson(JsonObject jsonObject) {
        String[] messageFields = { "message", "msg", "log_message" };

        for (String field : messageFields) {
            if (jsonObject.has(field) && !jsonObject.get(field).isJsonNull()) {
                String message = jsonObject.get(field).getAsString();
                // Remove emojis if desired
                message = message.replaceAll("[^\\x00-\\x7F]", "");
                return message;
            }
        }

        return "No message";
    }

    private String extractProcessNameFromJson(JsonObject jsonObject) {
        String[] processFields = { "process_name", "processName", "script_name" };

        for (String field : processFields) {
            if (jsonObject.has(field) && !jsonObject.get(field).isJsonNull()) {
                String processName = jsonObject.get(field).getAsString();
                if (processName != null && !processName.equals("null") && !processName.trim().isEmpty()) {
                    return processName;
                }
            }
        }

        // Try to extract from message if process_name is not found
        if (jsonObject.has("message")) {
            String message = jsonObject.get("message").getAsString();
            return extractProcessNameFromText(message);
        }

        return "Unknown Process"; // Only as fallback
    }

    private Map<String, Object> extractAllAdditionalFields(JsonObject jsonObject) {
        Map<String, Object> additionalFields = new HashMap<>();

        // List of standard fields that we already extract
        Set<String> standardFields = Set.of("asctime", "timestamp", "levelname", "level",
                "message", "msg", "process_name", "processName",
                "script_name");

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            if (!standardFields.contains(key)) {
                JsonElement value = entry.getValue();
                if (value.isJsonNull()) {
                    additionalFields.put(key, null);
                } else if (value.isJsonPrimitive()) {
                    additionalFields.put(key, value.getAsString());
                } else {
                    additionalFields.put(key, value.toString());
                }
            }
        }

        return additionalFields;
    }

    private String extractLevelFromText(String line) {
        if (line.contains("ERROR"))
            return "ERROR";
        if (line.contains("WARNING"))
            return "WARNING";
        if (line.contains("INFO"))
            return "INFO";
        if (line.contains("DEBUG"))
            return "DEBUG";
        if (line.contains("CRITICAL"))
            return "CRITICAL";
        return "UNKNOWN";
    }

    private String extractProcessNameFromText(String message) {
        // Try to extract process name from common patterns in the message
        // if (message.contains("RedingtonProcess")) {
        // return "RedingtonProcess";
        // }
        if (message.contains("process automation script started")) {
            // Extract the process name from messages like "XYZ process automation script
            // started"
            Pattern pattern = Pattern.compile("(\\w+) process automation script started");
            var matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        if (message.contains("Process:")) {
            // Extract from "Process: XYZ" pattern
            Pattern pattern = Pattern.compile("Process:\\s*(\\w+)");
            var matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return "Unknown Process";
    }

    private LocalDateTime parseTimestamp(String timestampStr) {
        for (DateTimeFormatter formatter : TIMESTAMP_FORMATTERS) {
            try {
                // Handle the comma in milliseconds
                if (timestampStr.contains(",") && !timestampStr.contains(".")) {
                    timestampStr = timestampStr.replace(",", ".");
                }
                return LocalDateTime.parse(timestampStr, formatter);
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        // If all formatters fail, try with some cleaning
        try {
            // Remove milliseconds if present
            if (timestampStr.contains(".")) {
                timestampStr = timestampStr.substring(0, timestampStr.lastIndexOf('.'));
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timestampStr, formatter);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse timestamp: {}", timestampStr, e);
            return LocalDateTime.now();
        }
    }

    private LogPython createBasicLogEntry(String line) {
        LogPython log = new LogPython();
        log.setTimestamp(LocalDateTime.now());
        log.setLevel("UNKNOWN");
        log.setMessage(line);

        // Extract process name from the line content
        String processName = extractProcessNameFromText(line);
        log.setProcessName(processName);

        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("raw_line", line);
        log.setAdditionalFields(additionalFields);

        return log;
    }

    // UiPath Pattern Execution Time Calculation
    private void calculateExecutionTimesUiPathPattern(List<LogPython> logs) {
        if (logs.isEmpty())
            return;

        // Sort logs by timestamp
        List<LogPython> sortedLogs = logs.stream()
                .sorted(Comparator.comparing(LogPython::getTimestamp))
                .collect(Collectors.toList());

        // Debug: Print sorted logs with timestamps
        // System.out.println("=== SORTED LOGS ===");
        for (int i = 0; i < sortedLogs.size(); i++) {
            LogPython log = sortedLogs.get(i);
            // System.out.println(i + ": " + log.getTimestamp() + " - " + log.getLevel() + "
            // - " + log.getMessage());
        }

        // Find all execution sessions
        List<ExecutionSession> sessions = findExecutionSessions(sortedLogs);

        // System.out.println("Found " + sessions.size() + " execution sessions");

        // Process each session
        for (ExecutionSession session : sessions) {
            processExecutionSession(session, sortedLogs);
        }
    }

    private List<ExecutionSession> findExecutionSessions(List<LogPython> sortedLogs) {
        List<ExecutionSession> sessions = new ArrayList<>();
        int currentStartIndex = -1;

        for (int i = 0; i < sortedLogs.size(); i++) {
            LogPython log = sortedLogs.get(i);

            if (isExecutionStarted(log.getMessage())) {
                // If we find a start and we already have a session started, end the previous
                // one
                if (currentStartIndex != -1) {
                    sessions.add(new ExecutionSession(currentStartIndex, i - 1));
                }
                currentStartIndex = i;
            }

            if (isExecutionEnded(log.getMessage()) && currentStartIndex != -1) {
                sessions.add(new ExecutionSession(currentStartIndex, i));
                currentStartIndex = -1;
            }
        }

        // Handle the last session if it didn't end properly
        if (currentStartIndex != -1) {
            sessions.add(new ExecutionSession(currentStartIndex, sortedLogs.size() - 1));
        }

        return sessions;
    }

    private void processExecutionSession(ExecutionSession session, List<LogPython> sortedLogs) {
        int startIndex = session.startIndex;
        int endIndex = session.endIndex;

        LogPython startLog = sortedLogs.get(startIndex);
        LogPython endLog = sortedLogs.get(endIndex);

        PythonExecutionTime executionTime = new PythonExecutionTime();

        // Set timestamps with new field names
        executionTime.setStartTimeinLocalDateTime(startLog.getTimestamp());
        executionTime.setEndTimeinLocalDateTime(endLog.getTimestamp());

        // Calculate duration and set as string
        Duration duration = Duration.between(startLog.getTimestamp(), endLog.getTimestamp());
        executionTime.setTimeDifference(Long.toString(duration.getSeconds()));

        // Set dates with correct field name
        executionTime.setLogDateinLocalDate(startLog.getTimestamp().toLocalDate());
        executionTime.setCreatedDate(new Date());

        // Set process information
        executionTime.setProcessName(startLog.getProcessName());

        // Calculate metrics
        // calculateSessionMetrics(sortedLogs, startIndex, endIndex, executionTime);

        // Determine execution status with new field name
        String status = determineExecutionStatus(sortedLogs, startIndex, endIndex);
        executionTime.setProcessStatus(status);

        // System.out.println("Saving execution: " + executionTime.getProcessName() +
        // " - " + executionTime.getStartTimeinLocalDateTime() + " to " +
        // executionTime.getEndTimeinLocalDateTime() +
        // " - Duration: " + executionTime.getTimeDifference() + "s - Status: " +
        // status);

        pythonExecutionTimeRepository.save(executionTime);
    }

    // private void calculateSessionMetrics(List<LogPython> logs, int startIndex,
    // int endIndex, PythonExecutionTime executionTime) {
    // int totalInvoices = 0;
    // int successfulInvoices = 0;
    // int failedInvoices = 0;
    // List<String> errorMessages = new ArrayList<>();
    //
    // for (int i = startIndex; i <= endIndex; i++) {
    // LogPython log = logs.get(i);
    //
    // // Count invoice operations
    // if (log.getMessage().toLowerCase().contains("processing invoice")) {
    // totalInvoices++;
    // }
    // if (log.getMessage().toLowerCase().contains("uploaded pdf")) {
    // successfulInvoices++;
    // }
    // if (log.getMessage().toLowerCase().contains("failed to process invoice")) {
    // failedInvoices++;
    // }
    //
    // // Collect error messages
    // if ("ERROR".equalsIgnoreCase(log.getLevel())) {
    // errorMessages.add(log.getMessage());
    // }
    // }
    //
    // //executionTime.setTotalOperations(totalInvoices);
    // //executionTime.setSuccessfulOperations(successfulInvoices);
    // // executionTime.setFailedOperations(failedInvoices);
    // executionTime.setErrorMessages(errorMessages);
    // }

    private String determineExecutionStatus(List<LogPython> logs, int startIndex, int endIndex) {
        LogPython endLog = logs.get(endIndex);

        if (isExecutionEnded(endLog.getMessage())) {
            // Check if there were any errors
            for (int i = startIndex; i <= endIndex; i++) {
                if ("ERROR".equalsIgnoreCase(logs.get(i).getLevel())) {
                    return "Successfully Ended"; // Match UiPath pattern
                }
            }
            return "Successfully Ended"; // Match UiPath pattern
        } else {
            return "Failed to End"; // Match UiPath pattern
        }
    }

    private boolean isExecutionStarted(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("started");
    }

    private boolean isExecutionEnded(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("ended");
    }

    // Helper class to represent execution sessions
    private static class ExecutionSession {
        int startIndex;
        int endIndex;

        ExecutionSession(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    public JSONObject getPythonUtilisation(RequestPythonUtilisation request) {
        List<PythonExecutionTime> listProcess = new ArrayList<>();
        JSONObject obj = new JSONObject();

        long daysDiff = ChronoUnit.DAYS.between(request.getStartTime(), request.getEndTime()) + 1;

        LocalDate startDate = request.getStartTime();

        // System.out.println("Date range: " + request.getStartTime() + " to " +
        // request.getEndTime());
        // //System.out.println("Total days: " + daysDiff);
        // //System.out.println("Process name: " + request.getProcessName());

        for (int i = 1; i <= daysDiff; i++) {
            String days = "day" + i;
            LocalDate currentDate = startDate.plusDays(i - 1);

            // System.out.println("Processing " + days + " - Date: " + currentDate);

            // Convert LocalDate -> Date (start of day / end of day)
            Date startOfDay = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(currentDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            Query query = new Query();
            query.addCriteria(Criteria.where("processName").is(request.getProcessName()));
            query.addCriteria(Criteria.where("logDateinLocalDate").is(currentDate));

            listProcess = this.mongoTemplate.find(query, PythonExecutionTime.class);

            // System.out.println("Found " + listProcess.size() + " executions for date: " +
            // currentDate);

            long singleDayProcessUtilisation = 0L;
            for (PythonExecutionTime pet : listProcess) {
                // //System.out.println("Execution: " + pet.getProcessName() +
                // " - Date: " + pet.getLogDateinLocalDate() +
                // " - Duration: " + pet.getTimeDifference());
                try {
                    singleDayProcessUtilisation += Long.parseLong(pet.getTimeDifference());
                } catch (NumberFormatException e) {
                    // System.err.println("Invalid time difference format: " +
                    // pet.getTimeDifference());
                }
            }

            obj.put(days, singleDayProcessUtilisation);
            // System.out.println("Utilisation for " + days + " (" + currentDate + "): " +
            // singleDayProcessUtilisation + " seconds");
        }

        // System.out.println("Final utilisation object: " + obj.toString());
        return obj;
    }

    public String processAndSaveLogs(String name) {
        List<String> list = extractLines(name);
        List<String> jsonList = extractJsonObjects(list);
        saveLogsToDatabase(jsonList);
        calculateExecutionTimes();
        return "Saved the log file and calculated the utilisation";
    }

    // --- Step 1: Extract lines from input
    private List<String> extractLines(String content) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner = new Scanner(content)) {
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
        }
        log.info("Total logs found: {}", list.size());
        return list;
    }

    // --- Step 2: Extract proper JSONs from logs
    private List<String> extractJsonObjects(List<String> list) {
        List<String> finalList = new ArrayList<>();
        for (String line : list) {
            String properJson = "{" + line.substring(line.lastIndexOf("\"message\""));
            finalList.add(properJson);
        }
        log.info("Total JSON objects found: {}", finalList.size());
        return finalList;
    }

    // --- Step 3: Parse and Save logs
    private void saveLogsToDatabase(List<String> jsonList) {
        for (String json : jsonList) {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Log log = buildLogObject(jsonObject);
            logRepository.save(log);
        }
        log.info("All Logs are saved to the db");
    }

    private Log buildLogObject(JsonObject jsonObject) {
        Log log = new Log();
        String result = jsonObject.get("timeStamp").getAsString().split("\\.")[0];
        LocalDateTime dateTime1 = LocalDateTime.parse(result);
        log.setTimeStamp(dateTime1);
        log.setLogStatus("uploaded");
        log.setMessage(jsonObject.get("message").getAsString());
        log.setLevel(jsonObject.get("level").getAsString());
        log.setLogType(jsonObject.get("logType").getAsString());
        log.setFingerprint(jsonObject.get("fingerprint").getAsString());
        log.setWindowsIdentity(jsonObject.get("windowsIdentity").getAsString());
        log.setMachineName(jsonObject.get("machineName").getAsString());

        Query query = new Query(Criteria.where("machineName").is(jsonObject.get("machineName").getAsString()));
        BotModel bot = mongoTemplate.findOne(query, BotModel.class);
        if (bot != null) {
            log.setBotId(bot.getBotId() != null ? bot.getBotId() : null);
            log.setBotName(bot.getBotName());
        }

        log.setProcessName(jsonObject.get("processName").getAsString());
        log.setProcessVersion(jsonObject.get("processVersion").getAsString());
        log.setJobId(jsonObject.get("jobId").getAsString());
        log.setRobotName(jsonObject.get("robotName").getAsString());
        log.setMachineId(jsonObject.get("machineId").getAsString());

        if (!log.getLogType().contains("User"))
            log.setFileName(jsonObject.get("fileName").getAsString());
        if (log.getMessage().contains("execution started"))
            log.setInitiatedBy(jsonObject.get("initiatedBy").getAsString());
        if (log.getMessage().contains("execution ended")) {
            log.setTotalExecutionTimeInSeconds(jsonObject.get("totalExecutionTimeInSeconds").getAsString());
            log.setTotalExecutionTime(jsonObject.get("totalExecutionTime").getAsString());
        }

        return log;
    }

    // --- Step 4: Calculate Execution Times per Bot
    private void calculateExecutionTimes() {
        MongoCollection<?> mongoCollection = mongoTemplate.getCollection("Log");
        DistinctIterable<String> distinctIterable = mongoCollection.distinct("machineName", String.class);
        MongoCursor<String> cursor = distinctIterable.iterator();

        List<String> botList = new ArrayList<>();
        while (cursor.hasNext()) {
            botList.add(cursor.next());
        }

        for (String botMachine : botList) {
            processExecutionForBot(botMachine);
        }
    }

    private void processExecutionForBot(String botMachine) {
        Query query = new Query();
        query.addCriteria(Criteria.where("machineName").is(botMachine));
        query.addCriteria(Criteria.where("logStatus").is("uploaded"));
        List<Log> scenarioList = mongoTemplate.find(query, Log.class);

        log.info("Processing bot: {} | Total logs: {}", botMachine, scenarioList.size());

        // Mark logs as calculated
        for (Log log : scenarioList) {
            Query q = new Query(Criteria.where("_id").is(log.getFingerprint()));
            Update update = new Update().set("logStatus", "calculated");
            mongoTemplate.findAndModify(q, update, Log.class);
        }

        computeExecutionScenarios(scenarioList, botMachine);
    }

    private void computeExecutionScenarios(List<Log> scenarioList, String botMachine) {
        int startedCount = 0;
        for (Log log : scenarioList) {
            if (log.getMessage().contains("execution started"))
                startedCount++;
        }
        log.info("Started logs: {}", startedCount);

        int startedSoFar = 0;
        for (int k = 0; k < scenarioList.size(); k++) {
            Log currentLog = scenarioList.get(k);
            if (currentLog.getMessage().contains("execution started")) {
                startedSoFar++;
                handleExecutionBlock(scenarioList, k, startedCount, startedSoFar, botMachine);
            }
        }
    }

    private void handleExecutionBlock(List<Log> scenarioList, int startIdx, int totalStarted, int startedSoFar,
            String botMachine) {
        ExecutionTime processDetails = new ExecutionTime();
        Log startLog = scenarioList.get(startIdx);
        LocalDateTime startTime = startLog.getTimeStamp();

        processDetails.setStartTimeinLocalDateTime(startTime);
        processDetails.setMachineName(startLog.getMachineName());
        processDetails.setBotName(startLog.getBotName());
        processDetails.setProcessName(startLog.getProcessName());
        processDetails.setProcessStatus("Started");
        processDetails.setJobId(startLog.getJobId());
        processDetails.setLogDateinLocalDate(LocalDate.from(startTime));

        // handle final log or mid ones
        if (startedSoFar != totalStarted) {
            for (int m = startIdx + 1; m < scenarioList.size(); m++) {
                Log nextLog = scenarioList.get(m);
                if (nextLog.getMessage().contains("execution started")) {
                    handleStartedEndedScenario(scenarioList, processDetails, startIdx, m);
                    break;
                }
            }
        } else {
            handleFinalExecutionScenario(scenarioList, processDetails, startIdx);
        }

        executionTimeRepository.save(processDetails);
    }

    private void handleStartedEndedScenario(List<Log> scenarioList, ExecutionTime processDetails, int startIdx,
            int endIdx) {
        Log startLog = scenarioList.get(startIdx);
        Log endLog = scenarioList.get(endIdx - 1);

        if (endLog.getMessage().contains("execution ended")) {
            processDetails.setEndTimeinLocalDateTime(endLog.getTimeStamp());
            Duration duration = Duration.between(startLog.getTimeStamp(), endLog.getTimeStamp());
            processDetails.setTimeDifference(Long.toString(duration.toSeconds()));
            processDetails.setProcessStatus("Successfully Ended");

            List<String> errorMessages = new ArrayList<>();
            for (int n = startIdx + 1; n < endIdx - 1; n++) {
                if (scenarioList.get(n).getLevel().contains("Error")) {
                    errorMessages.add(scenarioList.get(n).getMessage());
                    processDetails.setProcessStatus("Ended with Error");
                }
            }
            processDetails.setErrorMessage(errorMessages);
        } else {
            processDetails.setEndTimeinLocalDateTime(endLog.getTimeStamp());
            Duration duration = Duration.between(startLog.getTimeStamp(), endLog.getTimeStamp());
            processDetails.setTimeDifference(Long.toString(duration.toSeconds()));
            processDetails.setProcessStatus("Failed to End");
        }
    }

    private void handleFinalExecutionScenario(List<Log> scenarioList, ExecutionTime processDetails, int startIdx) {
        Log startLog = scenarioList.get(startIdx);
        Log lastLog = scenarioList.get(scenarioList.size() - 1);

        processDetails.setEndTimeinLocalDateTime(lastLog.getTimeStamp());
        Duration duration = Duration.between(startLog.getTimeStamp(), lastLog.getTimeStamp());
        processDetails.setTimeDifference(Long.toString(duration.toSeconds()));

        if (lastLog.getMessage().contains("execution ended")) {
            processDetails.setProcessStatus("Successfully Ended");
        } else {
            processDetails.setProcessStatus("Failed to End");
        }
    }

    public List<ExecutionTimeDTO> getDetailsLogByDateRange(String botName, String startDate, String endDate) {
        LocalDate sDate = LocalDate.parse(startDate);
        LocalDate eDate = LocalDate.parse(endDate);

        // Query ExecutionTime collection instead of Log collection
        Query query = new Query();
        query.addCriteria(Criteria.where("botName").is(botName));
        query.addCriteria(Criteria.where("logDateinLocalDate")
                .gte(sDate)
                .lte(eDate));

        List<ExecutionTime> executionTimes = mongoTemplate.find(query, ExecutionTime.class);
        return executionTimeMapper.toDTOList(executionTimes);
    }

}