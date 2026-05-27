## Todo List — User Stories & Acceptance Criteria

**US1: Add a todo item**
> As a user, I want to add a todo item so I can track something I need to do.

- Given a valid title, when I add it, then it appears in my list with status `pending`
- Given a blank title, when I add it, then it is rejected with an error


**US2: Complete a todo item**
> As a user, I want to mark a todo as done so I know I've finished it.

- Given a `pending` todo, when I complete it, then its status becomes `done`
- Given a `done` todo, when I complete it again, then nothing changes (idempotent)


**US3: List todos**
> As a user, I want to see all my todos so I know what's outstanding.

- Given no todos, when I list them, then I get an empty list
- Given some todos, when I list them, then I see all of them regardless of status


**US4: Delete a todo**
> As a user, I want to delete a todo so I can remove something added by mistake.

- Given an existing todo, when I delete it, then it no longer appears in the list
- Given a non-existent todo id, when I delete it, then it returns an error

