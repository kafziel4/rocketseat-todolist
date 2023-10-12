package br.com.keoma.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.keoma.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity<TaskModel> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var userId = (UUID) request.getAttribute("userId");
    taskModel.setUserId(userId);

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A data de início / data de término deve ser maior que a data atual");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "A data de início deve ser menor do que a data de término");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var userId = (UUID) request.getAttribute("userId");
    var tasks = this.taskRepository.findByUserId(userId);
    return tasks;
  }

  @PatchMapping("/{id}")
  public TaskModel update(@PathVariable UUID id, @RequestBody TaskModel taskModel) {
    var task = this.taskRepository.findById(id).orElse(null);
    if (task == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var updatedTask = this.taskRepository.save(task);
    return updatedTask;
  }
}
