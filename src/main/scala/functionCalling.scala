import com.github.tototoshi.csv.CSVReader

import java.io.File
import scala.concurrent.Future

object functionCalling {
  //val reader = CSVReader.open(new File("./openAIFiles/verksted3.csv"))

  def numberOfRegistredCompanies: String = {
    "13"
  }

  def numberOfDifferentCertifications: String = {
    "10"
  }

  def mostCommonCertification: String = {
    "KONTROLLORGAN01B"
  }

  def registeredIdentifiers: String = {
    "874170852,974170853"
  }

  def numberOfCompaniesMissingWebsites: String = {
    "1"
  }

  def mostCommonCEOFirstMame: String = {
    "Jake"
  }

  def mostPopularYearWorkshopIncorporate: String = {
    "2020"
  }
}
