package com.southpawfishel.todoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.rekotlin.Action
import org.rekotlin.StateType
import org.rekotlin.Store
import org.rekotlin.StoreSubscriber

data class Todo(val name: String, val complete: Boolean) {
    companion object {
        val nameLens: Lens<Todo, String> = object : Lens<Todo, String> {
            override fun get(parent: Todo) = parent.name
            override fun set(parent: Todo, newChild: String) = parent.copy(name = newChild)
        }

        val completeLens: Lens<Todo, Boolean> = object : Lens<Todo, Boolean> {
            override fun get(parent: Todo) = parent.complete
            override fun set(parent: Todo, newChild: Boolean) = parent.copy(complete = newChild)
        }
    }
}

data class AppState(val todos: Map<String, List<Todo>>) : StateType {
    companion object {
        val todosLens: Lens<AppState, Map<String, List<Todo>>> = object : Lens<AppState, Map<String, List<Todo>>> {
            override fun get(parent: AppState): Map<String, List<Todo>> = parent.todos
            override fun set(parent: AppState, newChild: Map<String, List<Todo>>) = AppState(newChild)
        }
    }
}

interface LensSetActionProtocol : Action {
    fun updatedState(previousState: AppState) : AppState
}

data class LensSetAction<SubStateType>(val lens: Lens<AppState, SubStateType>,
                                       val newSubState: SubStateType) : LensSetActionProtocol {
    override fun updatedState(previousState: AppState): AppState {
        return lens.set(previousState, newSubState)
    }
}

fun mainReducer(action: Action, state: AppState?): AppState {
    var newState = state!!

    when(action) {
        is LensSetAction<*> -> {
            newState = (action as LensSetActionProtocol).updatedState(previousState = newState)
        }
    }

    return newState
}

val mainStore = Store(
    reducer = ::mainReducer,
    state = AppState(todos = mapOf(
        "home" to listOf(
            Todo("Buy dog food", false),
            Todo("Do laundry", true)
        ),
        "work" to listOf(
            Todo("Something something scrum", false),
            Todo("1 on 1 with so and so", false)
        )
    ))
)

class MainActivity : AppCompatActivity(), StoreSubscriber<AppState> {

    private var todosLens: Lens<AppState, List<Todo>>
    private var todos: List<Todo> = listOf()

    init {
        // TODO: The view/activity shouldn't know about the path to its own todos
        // TODO: Move this somewhere better
        val homeLens = KeyLens.forKey<String, List<Todo>>("home")
        todosLens = AppState.todosLens * homeLens
        todos = todosLens.get(mainStore.state)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            createNewTodo()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = TodoRecyclerAdapter(todosLens = todosLens)

        mainStore.subscribe(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainStore.unsubscribe(this)
    }

    override fun newState(state: AppState) {
        updateView(state)
    }

    private fun createNewTodo() {
        mainStore.dispatch(LensSetAction(
            lens = todosLens,
            newSubState = todos + Todo(name = "test todo", complete = false)
        ))
    }

    private fun updateView(newState: AppState) {
        todos = todosLens.get(newState)
        (recycler.adapter as TodoRecyclerAdapter).bindTodos(newState)
        (recycler.adapter as TodoRecyclerAdapter).notifyDataSetChanged()
    }
}
