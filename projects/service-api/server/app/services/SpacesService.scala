package services

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.Configuration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import models.services.spaces.BucketFile

import java.io.File
import java.net.URL
import javax.inject.Inject

sealed trait SpaceKind extends EnumEntry

object SpaceKind extends Enum[SpaceKind] with PlayJsonEnum[SpaceKind] {

  val values = findValues

  case object IMAGE           extends SpaceKind
  case object FLASHCARD_AUDIO extends SpaceKind

}

class SpacesService @Inject()(config: Configuration) {
  import services.SpaceKind._

  private val imageSpaceName: String     = config.get[String]("digitalOcean.imageSpaceName")
  private val imageSpaceEndpoint: String = config.get[String]("digitalOcean.imageSpaceEndpoint")
  private val audioSpaceName: String     = config.get[String]("digitalOcean.audioSpaceName")
  private val audioSpaceEndpoint: String = config.get[String]("digitalOcean.audioSpaceEndpoint")
  private val doAccessKey: String        = config.get[String]("digitalOcean.doAccessKey")
  private val doSecretKey: String        = config.get[String]("digitalOcean.doSecretKey")

  private val awsCreds = new BasicAWSCredentials(doAccessKey, doSecretKey)
  private val s3ClientImageBucket = AmazonS3ClientBuilder
    .standard()
    .withEndpointConfiguration(new EndpointConfiguration(imageSpaceEndpoint, imageSpaceEndpoint.split('.')(0)))
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .build()
  private val s3ClientAudioBucket = AmazonS3ClientBuilder
    .standard()
    .withEndpointConfiguration(new EndpointConfiguration(audioSpaceEndpoint, audioSpaceEndpoint.split('.')(0)))
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .build()

  def uploadFileToBucket(objectName: String, file: File, bucket: SpaceKind): BucketFile = {
    val (s3Client, bucketName) = bucket match {
      case IMAGE           => (s3ClientImageBucket, imageSpaceName)
      case FLASHCARD_AUDIO => (s3ClientAudioBucket, audioSpaceName)
    }

    s3Client.putObject(
      new PutObjectRequest(bucketName, objectName, file)
        .withCannedAcl(CannedAccessControlList.PublicRead))

    BucketFile(url = new URL(s"https://$bucketName/$objectName"), s3Object = s3Client.getObject(bucketName, objectName))
  }
}
