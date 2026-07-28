-- MySQL dump 10.14  Distrib 5.5.65-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: ghc
-- ------------------------------------------------------
-- Server version	5.5.65-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `commute`
--

DROP TABLE IF EXISTS `commute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `commute` (
  `commuteid` int(11) NOT NULL AUTO_INCREMENT,
  `latstart` float(10,6) DEFAULT NULL,
  `lngstart` float(10,6) DEFAULT NULL,
  `latend` float(10,6) DEFAULT NULL,
  `lngend` float(10,6) DEFAULT NULL,
  `dest` varchar(100) DEFAULT NULL,
  `latcurr` float(10,6) DEFAULT NULL,
  `lngcurr` float(10,6) DEFAULT NULL,
  `lastupdated` datetime DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`commuteid`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `commute`
--

LOCK TABLES `commute` WRITE;
/*!40000 ALTER TABLE `commute` DISABLE KEYS */;
INSERT INTO `commute` VALUES (33,12.967176,77.724678,12.956200,77.716797,'Apollo Hospital  in 1.49 kilometers',12.967551,77.716011,'2022-01-29 01:18:55',NULL);
/*!40000 ALTER TABLE `commute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospitals`
--

DROP TABLE IF EXISTS `hospitals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hospitals` (
  `hospid` int(11) NOT NULL,
  `hospname` varchar(100) DEFAULT NULL,
  `lat` float(10,6) DEFAULT NULL,
  `lng` float(10,6) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospitals`
--

LOCK TABLES `hospitals` WRITE;
/*!40000 ALTER TABLE `hospitals` DISABLE KEYS */;
INSERT INTO `hospitals` VALUES (1,'Rainbow Children Hospitals',12.975400,77.697800,'1802122'),(2,'Bengaluru Hospital',12.938700,77.554001,'111111'),(3,'Manipal Hospitals-Airport Road',12.958400,77.648804,'1800 102 5555'),(4,'Victoria Hospital',12.963500,77.573799,'080 2670 1150'),(5,'Command Hospital',12.964700,77.628098,'080 2530 1215'),(6,'Bowring and Lady Curzon Hospital',12.982200,77.604103,''),(7,'Central Leprosorium',12.981000,77.438103,''),(8,'E.S.I Hospital, Basavangudi',13.004600,77.554001,''),(9,'E.S.I Hospital, Indiranagar',12.971200,77.636902,''),(10,'E.S.I Hospital, Banashankari',12.931000,77.564697,''),(11,'Epidemic Diseases Hospital',12.986000,77.644302,''),(12,'General Hospital, Jayanagar',12.996100,77.569397,''),(13,'Govt. TB and CD Hospital',12.985001,77.643257,''),(14,'Indira Gandhi Institute of Child Health Hospital',12.903800,77.566299,''),(15,'Jayadeva Institute of Cardiology',12.918000,77.599197,''),(16,'Kidwai Memorial Institute of Oncology',12.937400,77.598000,''),(17,'Minto Ophthalmic Hospital',12.960900,77.571899,''),(18,'Nimhans Hospital',12.938900,77.594101,''),(19,'BGS Global Hospital',12.903000,77.497498,''),(20,'Manipal Hospital ',12.958400,77.648804,''),(21,'M.S. Ramaiah Memorial Hospital',13.028200,77.569901,''),(22,'Apollo Hospital ',12.956200,77.716797,''),(23,'Fortis Hospital',12.894800,77.598602,''),(24,'Vanivilas Hospital',12.961400,77.573997,'');
/*!40000 ALTER TABLE `hospitals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `userid` int(11) NOT NULL,
  `username` varchar(45) DEFAULT NULL,
  `lat` float(10,6) DEFAULT NULL,
  `lng` float(10,6) DEFAULT NULL,
  `lastupdated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-04-21 13:53:44
