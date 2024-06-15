import com.github.tototoshi.csv.CSVReader

import java.io.File
import scala.concurrent.Future

object statistic {
  //val reader = CSVReader.open(new File("./openAIFiles/verksted3.csv"))

  
  def numberOfRegistredCompanies: String = {
    "3"
  }

  def numberOfDifferentCertifications: String = {
    "10"
  }

  def mostCommonCertification: String = {
    "cert"
  }

  def numberOfCompaniesMissingWebsites: String = {
    "1"
  }

  def mostCommonCEOFirstMame: String = {
    "Jake"
  }

  def mostPopularYearWorkshopIncorporate: String = {
    "2000"
  }
}
