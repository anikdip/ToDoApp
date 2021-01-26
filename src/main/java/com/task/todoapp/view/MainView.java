package com.task.todoapp.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.task.todoapp.Entity.Todo;
import com.task.todoapp.Repository.TodoRepo;
import com.task.todoapp.component.TodoEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;

@Route("")
public class MainView extends VerticalLayout {

    @JsonIgnore
    private static final long serialVersionUID = 7564533820008054012L;

    private final TodoRepo repo;
    private final TodoEditor editor;
    private final Grid<Todo> grid;
    private final TextField filter;
    private final Button addNewBtn;

    public MainView(TodoRepo repo, TodoEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Todo.class);
        this.filter = new TextField();
        this.addNewBtn =
                new Button(
                        "New Todo",
                        VaadinIcon.PLUS.create()
                );

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "taskDate", "itemName", "description", "status");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);
        grid.addColumn(TemplateRenderer.<Todo>of(
                "<button on-click='handleUpdate'>Update</button>&nbsp;&nbsp;&nbsp;" +
                        "<button on-click='handleRemove'>Remove</button>")
                .withEventHandler("handleUpdate", todo -> {
                    editor.editTodo(todo);
                    grid.getDataProvider().refreshItem(todo);
                }).withEventHandler("handleRemove", todo -> {
                    ListDataProvider<Todo> dataProvider =
                            (ListDataProvider<Todo>) grid
                                    .getDataProvider();
                    dataProvider.getItems().remove(todo);
                    dataProvider.refreshAll();
                })).setHeader("Actions");


        filter.setPlaceholder("Filter by item name");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listTodos(e.getValue()));

        // Connect selected Todo to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editTodo(e.getValue());
        });

        // Instantiate and edit new Todo the new button is clicked
        addNewBtn.addClickListener(e -> editor.editTodo(new Todo(null, "", "", "")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listTodos(filter.getValue());
        });

        // Initialize listing
        listTodos(StringUtils.EMPTY);
    }

    private void listTodos() {
        grid.setItems(repo.findAll());
    }

    private void listTodos(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            listTodos();
        } else {
            grid.setItems(repo.findByItemNameStartsWithIgnoreCase(filterText));
        }
    }
}
