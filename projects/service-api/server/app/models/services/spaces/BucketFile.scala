package models.services.spaces

import com.amazonaws.services.s3.model.S3Object

import java.net.URL

case class BucketFile(
    url: URL,
    s3Object: S3Object
)
