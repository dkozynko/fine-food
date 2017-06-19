import akka.actor.ActorSystem

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import spray.client.pipelining._
import spray.httpx.Json4sSupport
import org.json4s.{DefaultFormats, Formats}
import spray.http._

class GoogleTranslateService {
	object GoogleTranslateProtocol extends Json4sSupport {
		override implicit def json4sFormats: Formats = DefaultFormats

		case class TranslateRequest(input_lang: String, output_lang: String, text: String)
		case class TranslateResponse(text: String)
	}

	// marshaller/unmarshaller implicits for the pipeline
	import GoogleTranslateProtocol._

	def translate(inputLanguage: String, outputLanguage: String, text: String): String = {
		implicit val system = ActorSystem("simple-spray-client")
		import system.dispatcher
		
		// real call is commented
		// def sendAndReceive = sendReceive

		def sendAndReceive = {
			(req:HttpRequest) => Promise.successful( // mock answer
				HttpResponse(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, "translated".getBytes))
			).future
		}

		val pipeline = (
			addHeader("Accept", "application/json")
				~> sendAndReceive
				~> unmarshal[TranslateResponse]
			)

		val response: Future[TranslateResponse] = pipeline {
			Post("https://api.google.com/translate", TranslateRequest(inputLanguage, outputLanguage, text))
		}

		Await.result(response, 300.milliseconds).text
	}
}
