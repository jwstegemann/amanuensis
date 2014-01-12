import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._
import akka.actor.{ActorLogging, Actor}

import amanuensis.api.StoryHttpService

import amanuensis.domain._
import amanuensis.core._

import amanuensis.api.exceptions._
import amanuensis.domain.Message
import amanuensis.domain.Severities._
import amanuensis.domain.MessageJsonProtocol._
import amanuensis.core.neo4j.Neo4JException
import spray.http._
import spray.routing._


class AmanuensisStoryApiSpec extends Specification with Specs2RouteTest with StoryHttpService with Core with CoreActors {
  
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  import StoryProtocol._

  //FIXME: externalize this (use just one for Service and Test)!
  implicit val amanuensisExceptionHandler = ExceptionHandler {
    case InternalServerErrorException(messages) => complete(InternalServerError, messages)
    case NotFoundException(message) => complete(NotFound, message)
    case ValidationException(messages) => complete(PreconditionFailed, messages)
    case Neo4JException(message) => {
      //log.error(s"Neo4J-error: $message")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))     
    }
    case t: Throwable => {
      //log.error(t, "Unexpected error:")
      complete(InternalServerError, Message("An unexpected Error occured. Please inform your system administrator.", `ERROR`))
    }
  }

  val testTitle1 = "Autotest_1"
  val testContent1 = "Content Autotest_1x"
  val testContent1Updated = "Updated content Autotest_1"
  var testId1 = ""

  val testTitle2 = "Autotest_2"
  val testContent2 = "Content Autotest_1"
  var testId2 = ""

  val testSlot1 = "Autotest_Slot_1"
  val testSlot2 = "Autotest_Slot_2"


  "The StoryService" should {

    sequential

    "create a new story" in {
      Post("/story",Story(None, testTitle1, testContent1)) ~> storyRoute ~> check {
        responseAs[StoryInfo] must beLike {
          case StoryInfo(id,title) => {
            testId1 = id
            title ===  testTitle1
          }
        }
      }
    }
    
    "update an existing story" in {
      Put(s"/story/$testId1",Story(Some(testId1), testTitle1, testContent1Updated)) ~> storyRoute ~> check {
        status === OK
      }
    }

    "retrieve an existing story" in {
      Get(s"/story/$testId1") ~> storyRoute ~> check {
        responseAs[StoryContext] must beLike {
          case StoryContext(story, inSlots, outSlots) => {
            story.id.get === testId1
            story.title === testTitle1
            // story.content === testContent1Updated // maybe to fast?
            (inSlots must be empty)
            (outSlots must be empty)
          }
        }
      }
    }

    "create a new story in slot" in {
      Post(s"/story/$testId1/$testSlot1",Story(None, testTitle2, testContent2)) ~> storyRoute ~> check {
        responseAs[StoryInfo] must beLike {
          case StoryInfo(id,title) => {
            testId2 = id
            title ===  testTitle2
          }
        }
      }
    }

    "find the new created story in slot" in {
      Get(s"/story/$testId1/$testSlot1") ~> storyRoute ~> check {
        responseAs[Seq[StoryInfo]] === (StoryInfo(testId2, testTitle2) :: Nil)
      }
    }

    "add existing story to a new slot" in {
      Put(s"/story/$testId2/$testSlot2/$testId1") ~> storyRoute ~> check {
        status === OK
      }      
    }

    "find the added story in slot" in {
      Get(s"/story/$testId2/$testSlot2") ~> storyRoute ~> check {
        responseAs[Seq[StoryInfo]] === (StoryInfo(testId1, testTitle1) :: Nil)
      }      
    }

    "return slots right in retrieve" in {
      Get(s"/story/$testId1") ~> storyRoute ~> check {
        responseAs[StoryContext] must beLike {
          case StoryContext(story, inSlots, outSlots) => {
            story.id.get === testId1
            inSlots === (Slot(testSlot2) :: Nil)
            outSlots === (Slot(testSlot1) :: Nil)
          }
        }
      }      
    }

    "remove story from slot" in {
      Delete(s"/story/$testId1/$testSlot1/$testId2") ~> storyRoute ~> check {
        status === OK 
      }      
    }

    "do not find removed story in slot anymore" in {
      Get(s"/story/$testId1/$testSlot1") ~> storyRoute ~> check {
        responseAs[Seq[StoryInfo]] must be empty
      }      
    }

    "delete existing stories including relations" in {
      Delete(s"/story/$testId1") ~> storyRoute ~> check {
        status === OK
      }      
      Delete(s"/story/$testId2") ~> storyRoute ~> check {
        status === OK
      }      
    }

    "do not find deleted stories anymore" in {
      Get(s"/story/$testId1") ~> sealRoute(storyRoute) ~> check {
        status === NotFound
      }        
      Get(s"/story/$testId2") ~> sealRoute(storyRoute) ~> check {
        status === NotFound
      }        
    }

  }
}