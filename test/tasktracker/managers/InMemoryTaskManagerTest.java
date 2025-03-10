package tasktracker.managers;

class InMemoryTaskManagerTest extends GeneralTaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager getTaskManager() {
        return new InMemoryTaskManager();
    }
}