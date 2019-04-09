package com.southpawfishel.todoapp

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.todo_recyclerview_item_row.view.*

class TodoRecyclerAdapter(private val todosLens: Lens<AppState, List<Todo>>) : RecyclerView.Adapter<TodoRecyclerAdapter.TodoHolder>() {
    private var todos: List<Todo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoRecyclerAdapter.TodoHolder {
        val inflatedView = parent.inflate(R.layout.todo_recyclerview_item_row, false)
        return TodoHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return todosLens.get(mainStore.state).size
    }

    override fun onBindViewHolder(holder: TodoRecyclerAdapter.TodoHolder, position: Int) {
        val indexLens = IndexLens.forIndex<Todo>(position)
        holder.bindTodo(todos[position], todosLens * indexLens)
    }

    fun bindTodos(state: AppState) {
        todos = todosLens.get(state)
    }

    class TodoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var todo: Todo? = null
        private var todoLens: Lens<AppState, Todo>? = null

        init {
            v.setOnClickListener(this)
        }

        fun bindTodo(todo: Todo, todoLens: Lens<AppState, Todo>) {
            this.todo = todo
            this.todoLens = todoLens
            view.textView.text = todo.name
            view.imageView.setImageResource(
                if (todo.complete) android.R.drawable.checkbox_on_background
                else android.R.drawable.checkbox_off_background
            )
        }

        override fun onClick(v: View) {
            val completedLens = Todo.completeLens
            val todoCompletedLens = todoLens!! * completedLens
            mainStore.dispatch( LensSetAction(
                lens = todoCompletedLens,
                newSubState = !completedLens.get(todo!!)
            ))
        }

        companion object {
            private val TODO_KEY = "TODO"
        }
    }
}