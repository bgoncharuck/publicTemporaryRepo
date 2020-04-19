interface progressTempalte {
  var current: Byte
  val max: Byte
}

data class Progress(val _current: Byte, val _max: Byte): progressTempalte {
  override var current: Byte= _current
  override val max: Byte= _max
}

interface lesson {
  val text: String
  val exercise: String
  val test: String
}

class defaultLesson(): lesson {
  override val text: String= "Example lesson text"
  override val exercise: String= "Example lesson exercise"
  override val test: String= "Example lesson test"
}

abstract class courseT: /*патерн прототип*/ Cloneable {

  abstract var name: String
  abstract var description: String
  abstract var progress: progressTempalte
  abstract var lessons: List<lesson>

  // патерн state
  abstract fun saveState(state: courseState): courseState
  abstract fun loadState(state: courseState)
  abstract fun create(state: courseState): courseT
}

// однакові поля зі CourseT, але неприватні, маленький патерн - Делегант, його можна не вказувати, але щоб ти знала. Можеш в коді просто саме посилання над інтерфейсом лишити, для краси
// https://www.wikiwand.com/ru/Шаблон_делегирования
interface courseState {
  val name: String
  val description: String
  val progress: progressTempalte
  val lessons: List<lesson>
}

class dynamicCourseState(val _name: String, val _description: String, val _progress: progressTempalte, val _lessons: List<lesson>): courseState {

  override val name: String = _name
  override val description: String = _description
  override val progress: progressTempalte = _progress
  override val lessons: List<lesson> = _lessons
}

class defaultCourseState(): courseState {

  override val name: String= "Name of course not specifed"
  override val description: String= "Description of course not specifed"
  override val progress: progressTempalte= Progress(0,10)
  override val lessons: List<lesson> = listOf(defaultLesson(), defaultLesson(), defaultLesson())
}

class Course: courseT() {

  val defaultState: courseState= defaultCourseState()
  override var name: String= defaultState.name
  override var description: String= defaultState.description
  override var progress: progressTempalte= defaultState.progress
  override var lessons: List<lesson> = defaultState.lessons


  override fun saveState(state: courseState): courseState= dynamicCourseState(name, description, progress, lessons)

  override fun loadState(state: courseState) {
    name= state.name
    description= state.description
    progress= state.progress
    lessons= state.lessons
  }

  // у котліні замість стрілочки просто = як у змінних
  override public fun clone(): courseT= super.clone() as Course

  // патерн прототип, продовження
  override fun create(state: courseState): courseT {
    loadState(state)
    return this.clone()
  }

}

// Патерн Visitor
interface courseCreatorT {
  val prototype: courseT
  // знак питання в кінці означає, що результат може бути NULL
  fun accept(visitor: createTheCourseVisitor): courseT?
}

interface createTheCourseVisitor {
  fun visit(courseCreator: courseCreatorT): courseT?
}

class CourseCreator(): courseCreatorT {

  override val prototype: courseT= Course()
  override fun accept(visitor: createTheCourseVisitor): courseT?= visitor.visit(this)
}

interface userT: createTheCourseVisitor {
  val name: String
}

// open потрібен, щоб змогти наслідувати клас в Котліні
open class User(val _name: String): userT {
  override val name: String= _name

  override fun visit(courseCreator: courseCreatorT): courseT? {
    print("$name, you are not allowed to create a course.\n")
    return null
  }
}

open class Admin(val __name: String): User(__name) {

  protected var preparedCourse: courseState= defaultCourseState()

  fun prepareCourse(state: courseState) {
    preparedCourse= state
  }

  // Шаблони прототип, стан та відвідувач разом, так і напиши англійською
  override fun visit(courseCreator: courseCreatorT): courseT? = courseCreator.prototype.create(preparedCourse)
}

fun main() {
  val courseCreator: courseCreatorT= CourseCreator()
  val vasya: userT= User("Vasya")
  val lena: Admin= Admin("Helen of Troy")

  print("\n\ndoing some stuff with graphics...\n")
  print("\n")
  print("vasya wants to create a course\n")
  val dreamOfVasya: courseT?= courseCreator.accept(vasya)
  print("vasya's course is $dreamOfVasya")
  print("\n")
  print("admin comes in\n")
  print("admin preparing a course\n")
  lena.prepareCourse(defaultCourseState())
  print("admin prepared a course and wants to publish it\n")
  val publishedCourse: courseT?= courseCreator.accept(lena)
  print("course was posted as \"${publishedCourse?.name}\"\n")
  print("\n")

  print("\nPress anything to exit, my Lord.")
  readLine()
}
