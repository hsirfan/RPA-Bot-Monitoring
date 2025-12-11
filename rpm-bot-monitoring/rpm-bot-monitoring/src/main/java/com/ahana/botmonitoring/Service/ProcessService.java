package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Exception.CustomException;
import com.ahana.botmonitoring.Entity.ProcessModel;
import com.ahana.botmonitoring.Mapper.ProcessMapper;
import com.ahana.botmonitoring.Repository.ProcessRepository;
import com.ahana.botmonitoring.generated.model.AddProcessResponse;
import com.ahana.botmonitoring.generated.model.ProcessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProcessService {
    // Service for Process operations

    private final ProcessRepository processRepository;
    private final ProcessMapper processMapper;
    private final MongoTemplate mongoTemplateRef;

    public ProcessService(ProcessRepository processRepository, ProcessMapper processMapper,
            MongoTemplate mongoTemplateRef) {
        this.processRepository = processRepository;
        this.processMapper = processMapper;
        this.mongoTemplateRef = mongoTemplateRef;
    }

    public AddProcessResponse saveNewProcess(ProcessDTO processDTO) {
        ProcessModel process = processMapper.toEntity(processDTO);

        if (process.getProcessId() == null) {
            Integer maxProcessId = processRepository.findTopByOrderByProcessIdDesc()
                    .map(ProcessModel::getProcessId)
                    .orElse(0);
            process.setProcessId(maxProcessId + 1);
        }

        processRepository.save(process);

        AddProcessResponse response = new AddProcessResponse();
        response.setStatus("Successfully Added Process");
        response.setProcessName(process.getProcessName());
        response.setId(process.getProcessId());

        return response;
    }

    public AddProcessResponse updateProcess(ProcessDTO processDTO) {
        ProcessModel processModel = processMapper.toEntity(processDTO);

        if (processModel.getProcessId() == null) {
            throw new CustomException("Process ID is required for update.");
        }

        ProcessModel existingProcess = processRepository.findByProcessId(processModel.getProcessId())
                .orElseThrow(() -> new CustomException("Process ID " + processModel.getProcessId() + " not found."));

        existingProcess.setBotName(processModel.getBotName());
        existingProcess.setProductionMovedDate(processModel.getProductionMovedDate());
        existingProcess.setProcessRequirements(processModel.getProcessRequirements());
        existingProcess.setProcessOwnerName(processModel.getProcessOwnerName());
        existingProcess.setProcessOwnerContact(processModel.getProcessOwnerContact());
        existingProcess.setProcessOwnerMailID(processModel.getProcessOwnerMailID());
        existingProcess.setProcessTeamMember(processModel.getProcessTeamMember());
        processRepository.save(existingProcess);

        AddProcessResponse response = new AddProcessResponse();
        response.setStatus("Process Updated Successfully");
        response.setProcessName(existingProcess.getProcessName());
        response.setId(existingProcess.getProcessId());

        return response;

    }

    public List<ProcessDTO> getAllProcess() {
        return processMapper.toDTOList(processRepository.findAll());
    }

    public Optional<ProcessDTO> getNewProcess(Integer processId) {
        return processRepository.findByProcessId(processId)
                .map(processMapper::toDTO);
    }

    public String deleteProcess(Integer processId) {
        ProcessModel process = processRepository.findByProcessId(processId)
                .orElseThrow(() -> new CustomException("Process ID " + processId + " not found."));

        processRepository.deleteById(process.getId());
        return "Process Deleted Successfully";
    }

    public List<ProcessDTO> getProcessesByBotId(Integer botId) {
        return processMapper.toDTOList(processRepository.findByBotId(botId));
    }

    public List<ProcessDTO> getProcessDetails(Integer processId) {
        return processRepository.findByProcessId(processId)
                .map(processMapper::toDTO)
                .map(List::of)
                .orElse(List.of());
    }

    public List<ProcessDTO> getProcessForBot(Integer botId) {
        return processMapper.toDTOList(processRepository.findByBotId(botId));
    }

    public List<ProcessDTO> getProcessForBotByName(String botName) {
        // Use MongoTemplate to find processes where botName array contains the given
        // botName
        Query query = new Query();
        query.addCriteria(Criteria.where("botName").in(botName));
        List<ProcessModel> processes = mongoTemplateRef.find(query, ProcessModel.class);
        return processMapper.toDTOList(processes);
    }

}
