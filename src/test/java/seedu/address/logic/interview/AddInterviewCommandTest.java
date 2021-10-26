package seedu.address.logic.interview;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPositions.ADMIN_ASSISTANT;
import static seedu.address.testutil.TypicalPositions.HR_MANAGER;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandResult;
import seedu.address.logic.candidate.exceptions.CommandException;
import seedu.address.model.HrManager;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyHrManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.interview.Interview;
import seedu.address.model.person.Person;
import seedu.address.model.position.Position;
import seedu.address.model.position.Title;
import seedu.address.testutil.InterviewBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PositionBuilder;

class AddInterviewCommandTest {

    @Test
    public void execute_interviewAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingInterviewAdded modelStub = new ModelStubAcceptingInterviewAdded();
        Interview validInterview = new InterviewBuilder().build();
        Set<Index> indexSet = new HashSet<>();
        indexSet.add(INDEX_FIRST_PERSON);
        CommandResult commandResult = new AddInterviewCommand(validInterview, indexSet).execute(modelStub);

        assertEquals(String.format(AddInterviewCommand.MESSAGE_SUCCESS, validInterview.getDisplayString()),
                commandResult.getFeedbackToUser());
        assertEquals(Arrays.asList(validInterview), modelStub.interviewsAdded);
    }

    @Test
    public void constructor_nullInterview_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddInterviewCommand(null, null));
    }

    @Test
    public void execute_duplicateInterview_throwsCommandException() {
        Interview validInterview = new InterviewBuilder().build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(validInterview, new HashSet<>());
        ModelStubWithInterviewAndPosition modelStub = new ModelStubWithInterviewAndPosition(validInterview);
        assertThrows(CommandException.class, AddInterviewCommand.MESSAGE_DUPLICATE_INTERVIEW, () ->
                addInterviewCommand.execute(modelStub));
    }

    @Test
    public void execute_noPositionFound_throwsCommandException() {
        Interview validInterview = new InterviewBuilder().build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(validInterview, new HashSet<>());
        ModelStubWithNoPosition modelStub = new ModelStubWithNoPosition();

        assertThrows(CommandException.class, String.format(AddInterviewCommand.MESSAGE_NO_POSITION_FOUND,
                validInterview.getPosition().getTitle()), () -> addInterviewCommand.execute(modelStub));
    }

    @Test
    public void execute_invalidPersonIndex_throwsCommandException() {
        ModelStubWithObservable model = new ModelStubWithObservable();
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        Set<Index> temp = new HashSet<>();
        temp.add(outOfBoundIndex);
        Interview validInterview = new InterviewBuilder().withPosition(ADMIN_ASSISTANT).build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(validInterview, temp);

        assertThrows(CommandException.class,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, () -> addInterviewCommand.execute(model));
    }

    @Test
    public void execute_noSuchPosition_throwsCommandException() {
        ModelStubWithObservable model = new ModelStubWithObservable();
        Index index = Index.fromZeroBased(0);
        Set<Index> temp = new HashSet<>();
        temp.add(index);
        Interview interview = new InterviewBuilder()
                .withPosition(new Position(new Title("accountant"))).build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(interview, temp);

        assertThrows(CommandException.class, String.format(AddInterviewCommand.MESSAGE_NO_POSITION_FOUND,
                "accountant"), () -> addInterviewCommand.execute(model));
    }

    @Test
    public void execute_didNotApply_throwsCommandException() {
        ModelStubWithObservable model = new ModelStubWithObservable();
        Index index = Index.fromZeroBased(0);
        Set<Index> temp = new HashSet<>();
        temp.add(index);
        Set<Person> candidates = new HashSet<>();
        candidates.add(BENSON);
        Interview interview = new InterviewBuilder().withCandidates(candidates)
                .withPosition(ADMIN_ASSISTANT).build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(interview, temp);

        assertThrows(CommandException.class, String.format(AddInterviewCommand.MESSAGE_CANDIDATE_DID_NOT_APPLY,
                BENSON.getName(), ADMIN_ASSISTANT), () -> addInterviewCommand.execute(model));
    }

    @Test
    public void execute_positionClosed_throwsCommandException() {
        ModelStubWithObservable model = new ModelStubWithObservable();
        Index index = Index.fromZeroBased(0);
        Set<Index> temp = new HashSet<>();
        temp.add(index);
        Set<Person> candidates = new HashSet<>();
        Person person = new PersonBuilder(BENSON).withPositions(ADMIN_ASSISTANT.getTitle().fullTitle)
                .build();
        candidates.add(person);

        Interview validInterview = new InterviewBuilder().withCandidates(candidates)
                .withPosition(
                        new PositionBuilder().withTitle("Admin")
                        .withStatus(Position.PositionStatus.CLOSED).build())
                .build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(validInterview, temp);

        assertThrows(CommandException.class, String.format(AddInterviewCommand.MESSAGE_POSITION_CLOSED,
                "Admin"), () -> addInterviewCommand.execute(model));
    }

    @Test
    public void equals() {
        Interview validInterview = new InterviewBuilder().build();
        AddInterviewCommand addInterviewCommand = new AddInterviewCommand(validInterview, new HashSet<>());
        Interview otherValidInterview = new Interview(CommandTestUtil.VALID_POSITION_ADMIN,
                CommandTestUtil.VALID_CANDIDATES_SET, CommandTestUtil.VALID_LOCAL_DATE,
                CommandTestUtil.VALID_START_TIME, CommandTestUtil.VALID_DURATION,
                CommandTestUtil.VALID_STATUS_PENDING);
        AddInterviewCommand otherAddInterviewCommand = new AddInterviewCommand(otherValidInterview, new HashSet<>());
        assertFalse(otherAddInterviewCommand.equals(addInterviewCommand));
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Path getHrManagerCandidatesFilePath() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Path getHrManagerPositionsFilePath() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Path getHrManagerInterviewsFilePath() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setHrManagerCandidatesFilePath(Path hrManagerCandidatesFilePath) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setHrManagerPositionsFilePath(Path hrManagerPositionsFilePath) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setHrManagerInterviewsFilePath(Path hrManagerInterviewsFilePath) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setHrManager(ReadOnlyHrManager newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyHrManager getHrManager() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPosition(Position position) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePosition(Position target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPosition(Position position) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPosition(Position target, Position editedPosition) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Position> getFilteredPositionList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPositionList(Predicate<Position> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        public void deletePositionFromPerson(Position p) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasInterview(Interview toAdd) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deleteInterview(Interview target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addInterview(Interview toAdd) {
            throw new AssertionError("This method should not be called.");
        }


        @Override
        public void setInterview(Interview target, Interview editedInterview) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Interview> getFilteredInterviewList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredInterviewList(Predicate<Interview> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Person getPerson(Index index) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean isPositionClosed(Position toCheck) {
            throw new AssertionError("This method should not be called.");
        }

        public void deleteInterviewFromPerson(Interview i) {
            throw new AssertionError("This method should not be called.");
        }

        public void deletePersonFromInterview(Person p) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single interview.
     */
    private class ModelStubWithInterviewAndPosition extends ModelStub {
        private final Interview interview;

        ModelStubWithInterviewAndPosition(Interview interview) {
            requireNonNull(interview);
            this.interview = interview;
        }

        @Override
        public boolean hasPosition(Position position) {
            return true;
        }

        @Override
        public boolean hasInterview(Interview interview) {
            requireNonNull(interview);
            return this.interview.isSameInterview(interview);
        }

        @Override
        public boolean isPositionClosed(Position toCheck) {
            return false;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.observableArrayList();
        }
    }

    /**
     * A Model stub that has ObservableList fields.
     */
    private class ModelStubWithObservable extends ModelStub {
        private ObservableList<Person> emptyPersonList = FXCollections.observableArrayList();
        private ObservableList<Position> emptyPositionList = FXCollections.observableArrayList();

        ModelStubWithObservable() {
            emptyPersonList.add(BENSON);
            emptyPositionList.add(ADMIN_ASSISTANT);
            emptyPositionList.add(HR_MANAGER);
            Position closedPosition = new PositionBuilder().withTitle("Admin")
                    .withStatus(Position.PositionStatus.CLOSED).build();
            emptyPositionList.add(closedPosition);
        }

        @Override
        public ObservableList<Position> getFilteredPositionList() {
            return emptyPositionList;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return emptyPersonList;
        }

        @Override
        public Person getPerson(Index index) {
            return emptyPersonList.get(index.getZeroBased());
        }

        @Override
        public boolean hasPosition(Position position) {
            return emptyPositionList.contains(position);
        }

        @Override
        public boolean isPositionClosed(Position toCheck) {
            for (Position p : emptyPositionList) {
                if (p.isSamePosition(toCheck)) {
                    return p.getStatus() == Position.PositionStatus.CLOSED;
                }
            }
            return false;
        }

        @Override
        public boolean hasInterview(Interview toAdd) {
            return false;
        }

        @Override
        public void addInterview(Interview toAdd) {

        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {

        }
    }

    /**
     * A Model stub that contains no Position.
     */
    private class ModelStubWithNoPosition extends ModelStub {

        ModelStubWithNoPosition() {
        }

        @Override
        public boolean hasPosition(Position position) {
            return false;
        }

        @Override
        public boolean hasInterview(Interview toAdd) {
            return false;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.observableArrayList();
        }
    }

    /**
     * A Model stub that always accept the interview being added.
     */
    private class ModelStubAcceptingInterviewAdded extends ModelStub {
        final ArrayList<Interview> interviewsAdded = new ArrayList<>();
        private ObservableList<Person> personsList = FXCollections.observableArrayList();
        private ObservableList<Position> positionsList = FXCollections.observableArrayList();

        private ModelStubAcceptingInterviewAdded() {
            personsList.add(BENSON);
        }

        @Override
        public Person getPerson(Index index) {
            requireNonNull(index);
            return personsList.get(index.getZeroBased());
        }

        @Override
        public boolean hasPosition(Position position) {
            return true;
        }

        @Override
        public boolean hasInterview(Interview interview) {
            requireNonNull(interview);
            return interviewsAdded.stream().anyMatch(interview::isSameInterview);
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            //empty method that does nothing for the stub to always accept
        }

        @Override
        public void addInterview(Interview interview) {
            requireNonNull(interview);
            interviewsAdded.add(interview);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return personsList;
        }

        @Override
        public boolean isPositionClosed(Position toCheck) {
            return false;
        }

        @Override
        public ReadOnlyHrManager getHrManager() {
            return new HrManager();
        }
    }
}
