package com.task.todoapp.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.task.todoapp.Entity.Todo;
import com.task.todoapp.Repository.TodoRepo;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class TodoEditor extends VerticalLayout implements KeyNotifier {

    @JsonIgnore
    private static final long serialVersionUID = -1281659000155982040L;

    private final TodoRepo repository;

    private Todo todo;

    /* Fields to edit properties in Todo entity */
    TextField itemName = new TextField("Item Name");
    TextField description = new TextField("Description");
    DatePicker taskDate = new DatePicker("Date");
    Select<String> status = new Select<>();


    /* Action buttons */
    // TODO why more code?
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Todo> binder = new Binder<>(Todo.class);
    private ChangeHandler changeHandler;

    public TodoEditor(TodoRepo repository) {
        this.repository = repository;
        status.setLabel("Status");
        status.setItems("ToDo", "Pending", "Done");

        add(taskDate, itemName, description, status, actions);

        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editTodo(todo));
        setVisible(false);
    }

    void delete() {
        repository.delete(todo);
        changeHandler.onChange();
    }

    void save() {
        repository.save(todo);
        changeHandler.onChange();
    }

    public interface ChangeHandler {

        void onChange();
    }

    public final void editTodo(Todo c) {
        if (c == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = c.getId() != null;
        if (persisted) {
            // Find fresh entity for editing
            todo = repository.findById(c.getId()).get();
        } else {
            todo = c;
        }
        cancel.setVisible(persisted);

        // Bind todo properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(todo);

        setVisible(true);

        // Focus first name initially
        taskDate.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

}
