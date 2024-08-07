package bootiful.htmx;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class HtmxApplication {

    public static void main(String[] args) {
        SpringApplication.run(HtmxApplication.class, args);
    }
}

@Controller
@RequestMapping("/todos")
class TodoController {

    private final Set<Todo> todos = new ConcurrentSkipListSet<>(Comparator.comparingInt(Todo::id));

    TodoController() {
        for (var t : "read a book, go to the gym, learn HATEOAS".split(","))
            this.todos.add(Todos.todo(t));
    }

    @GetMapping
    String todos(Model model) {
        model.addAttribute("todos", this.todos);
        return "todos";
    }

    @PostMapping
    HtmxResponse add(@RequestParam("new-todo") String newTodo,
                     Model model) {
        this.todos.add(Todos.todo(newTodo));
        model.addAttribute("todos", this.todos);
        return HtmxResponse
                .builder()
                .view("todos :: todos")
                .view("todos :: todos-form")
                .build();
    }


    @ResponseBody
    @DeleteMapping(produces = MediaType.TEXT_HTML_VALUE, path = "/{todoId}")
    String delete(@PathVariable Integer todoId) {
        this.todos
                .stream()
                .filter(t -> t.id().equals(todoId))
                .forEach(this.todos::remove);
        return "";
    }

}

record Todo(Integer id, String title) {
}

class Todos {
    private static final AtomicInteger id = new AtomicInteger(0);

    static Todo todo(String title) {
        return new Todo(id.incrementAndGet(), title);
    }
}