package teammates.ui.webapi;

import org.junit.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.test.BaseTestCase;

public class GateKeeperTest extends BaseTestCase {

  private InstructorAttributes instructor;
  private CourseAttributes course;
  private final String PRIVILEGE_NAME = "canModifyCourse";
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
        () -> gateKeeper.verifyAccessible(instructor, course, PRIVILEGE_NAME));

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
  }

}
