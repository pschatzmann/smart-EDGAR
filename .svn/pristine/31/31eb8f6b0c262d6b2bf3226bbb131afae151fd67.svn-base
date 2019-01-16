In the US, all companies, foreign and domestic, are required to file registration statements, periodic reports, and other forms electronically through <a href="https://www.sec.gov/edgar.shtml"><strong>EDGAR</strong></a>. Anyone can access and download this information for free.

This goal of this project is to make this information accessible in an easy way so that it can be used by any Data Science functionality. It consists of the following core functionality

- [A Java API](https://pschatzmann.ch/edgar/docs/overview-summary.html) 
   - which provides Company information and Company filings from the [EDGAR](https://www.sec.gov/edgar.shtml) site
   - which implements a XBRL parser (which supports XBRL an iXBRL)
   - for Reporting on Summary Information
   - for Reporting on Company KPIs
- [REST Services](https://www.pschatzmann.ch/edgar/index.html) 
  - to access XBRL files
  - to access consolidated numerical information which are stored in a Database
  - which provides financial KPIs for a selected company 
- Automatic Download of the latest XBRL files and import them into a SQL database

The full solution is provided as [Docker image](https://hub.docker.com/r/pschatzmann/smart-edgar/") 

The Java Library can be installed with the help of Maven from the following repository http://software.pschatzmann.ch/repository/maven-public/ as 

		<dependency>
			<groupId>ch.pschatzmann</groupId>
			<artifactId>smart-edgar</artifactId>
			<version>LATEST</version>
		</dependency>


## Further Information
Further information can be found in <a href="https://www.pschatzmann.ch/home/category/edgar/">my posts</a>