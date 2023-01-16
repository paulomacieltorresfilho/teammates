package teammates.ui.webapi;

import org.junit.Test;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.test.BaseTestCase;

public class GateKeeperTest extends BaseTestCase {

  private InstructorAttributes instructor;
  private CourseAttributes course;
  private final String PRIVILEGE_NAME = "canviewstudentinsection";
  private GateKeeper gateKeeper;

  private Exception exception;
  private String message;

  private void runMethod() throws UnauthorizedAccessException {
    gateKeeper.verifyAccessible(instructor, course, PRIVILEGE_NAME);
  }

  @Test
  public void test_verifyAccessible() {

    gateKeeper = GateKeeper.inst();

    ______TS("without instructor");
    exception = assertThrows(
        UnauthorizedAccessException.class,
        () -> runMethod());

    message = exception.getMessage();
    assertEquals(message, "Trying to access system using a non-existent instructor entity");

    ______TS("with instructor without course ID");
    instructor = InstructorAttributes.builder("", "instructor@mail.com").build();
    instructor.setCourseId(null);

    exception = assertThrows(
        UnauthorizedAccessException.class,
        () -> runMethod());

    message = exception.getMessage();
    assertEquals(message, "Trying to access system using a non-existent instructor's course ID entity");

    ______TS("with course equals null");
    instructor = InstructorAttributes.builder("courseId1", "instructor@mail.com").build();

    exception = assertThrows(
        UnauthorizedAccessException.class,
        () -> runMethod());

    message = exception.getMessage();
    assertEquals(message, "Trying to access system using a non-existent course entity");

    ______TS("with instructor.courseId and course.id not the same");
    instructor = InstructorAttributes.builder("courseId1Ins", "instructor@mail.com").build();
    course = CourseAttributes.builder("courseId1Cou").build();

    exception = assertThrows(
        UnauthorizedAccessException.class,
        () -> runMethod());

    message = exception.getMessage();
    assertEquals(message, "Course [" + course.getId() + "] is not accessible to instructor ["
        + instructor.getEmail() + "]");

    ______TS("instructor without course or section privilege");
    instructor = InstructorAttributes.builder("courseId1", "instructor@mail.com").build();
    course = CourseAttributes.builder("courseId1").build();

    instructor.getPrivileges().updatePrivilege(PRIVILEGE_NAME, false);
    instructor.getPrivileges().updatePrivilege("sectionTest", PRIVILEGE_NAME, false);

    exception = assertThrows(
        UnauthorizedAccessException.class,
        () -> runMethod());

    assertEquals(exception.getMessage(), "Course [" + course.getId() + "] is not accessible to instructor ["
        + instructor.getEmail() + "] for privilege [" + PRIVILEGE_NAME + "]");

    ______TS("instructor without section privilege but with course privilege");
    instructor = InstructorAttributes.builder("courseId1", "instructor@mail.com").build();
    course = CourseAttributes.builder("courseId1").build();

    instructor.getPrivileges().updatePrivilege(PRIVILEGE_NAME, true);
    instructor.getPrivileges().updatePrivilege("sectionTest", PRIVILEGE_NAME, false);

    try {
      runMethod();
    } catch (UnauthorizedAccessException e) {
      fail("Method was not supposed to throw UnauthorizedAccessException");
    }

    ______TS("instructor without course privilege but with section privilege");
    instructor = InstructorAttributes.builder("courseId1", "instructor@mail.com").build();
    course = CourseAttributes.builder("courseId1").build();

    instructor.getPrivileges().updatePrivilege(PRIVILEGE_NAME, false);
    instructor.getPrivileges().updatePrivilege("sectionTest", PRIVILEGE_NAME, true);

    try {
      runMethod();
    } catch (UnauthorizedAccessException e) {
      fail("Method was not supposed to throw UnauthorizedAccessException");
    }

    ______TS("instructor with both course and section privileges");
    instructor = InstructorAttributes.builder("courseId1", "instructor@mail.com").build();
    course = CourseAttributes.builder("courseId1").build();

    instructor.getPrivileges().updatePrivilege(PRIVILEGE_NAME, true);
    instructor.getPrivileges().updatePrivilege("sectionTest", PRIVILEGE_NAME, true);

    try {
      runMethod();
    } catch (UnauthorizedAccessException e) {
      fail("Method was not supposed to throw UnauthorizedAccessException");
    }
  }
}
