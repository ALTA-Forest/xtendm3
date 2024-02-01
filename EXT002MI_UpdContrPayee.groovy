// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a contract payee in EXTCTP
// Transaction UpdContrPayee
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CPID - Payee ID
 * @param: CASN - Payee Number
 * @param: PYNM - Payee Name
 * @param: CF15 - Payee Role
 * @param: SHTP - Share Type
 * @param: CATF - Take From ID
 * @param: TFNM - Take From Name
 * @param: CATP - Take Priority
 * @param: CAAM - Amount
 * @param: CASA - Test Share
 * @param: PLVL - Level
 * @param: SLVL - Sub-Level
 * @param: PPID - Parent Payee ID
 * 
*/


import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

public class UpdContrPayee extends ExtendM3Transaction {
  private final MIAPI mi; 
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final LoggerAPI logger;
  
  Integer CONO
  String DIVI
  String inRVID
  int inCPID
  String inCASN
  String inPYNM
  int inCF15
  String inSHTP
  String inCATF
  String inTFNM
  int inCATP
  double inCAAM
  double inCASA
  int inPLVL
  int inSLVL
  int inPPID
  int inISAH
  String inTRCK
  
  
  // Constructor 
  public UpdContrPayee(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database;
     this.miCaller = miCaller;
     this.program = program;
     this.logger = logger;     
  } 
    
  public void main() {       
     // Set Company Number
     CONO = mi.in.get("CONO")      
     if (CONO == null || CONO == 0) {
        CONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     DIVI = mi.in.get("DIVI")
     if (DIVI == null || DIVI == "") {
        DIVI = program.LDAZD.DIVI
     }

     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Payee ID
     if (mi.in.get("CPID") != null) {
        inCPID = mi.in.get("CPID") 
     } else {
        inCPID = 0       
     }
      
     // Payee Number
     if (mi.in.get("CASN") != null) {
        inCASN = mi.in.get("CASN") 
     } else {
        inCASN = ""         
     }
    
     // Payee Name
     if (mi.in.get("PYNM") != null) {
        inPYNM = mi.in.get("PYNM") 
     } else {
        inPYNM = ""         
     }
 
     // Payee Role
     if (mi.in.get("CF15") != null) {
        inCF15 = mi.in.get("CF15") 
     } else {
        inCF15 = 0       
     }
     
     // Share Type
     if (mi.in.get("SHTP") != null) {
        inSHTP = mi.in.get("SHTP") 
     } else {
        inSHTP = ""         
     }
    
     // Take From ID
     if (mi.in.get("CATF") != null) {
        inCATF = mi.in.get("CATF") 
     } else {
        inCATF = ""         
     }    

     // Take From Name
     if (mi.in.get("TFNM") != null) {
        inTFNM = mi.in.get("TFNM") 
     } else {
        inTFNM = ""         
     }
    
     // Take Priority
     if (mi.in.get("CATP") != null) {
        inCATP = mi.in.get("CATP") 
     } else {
        inCATP = 0       
     }
    
     // Amount
     if (mi.in.get("CAAM") != null) {
        inCAAM = mi.in.get("CAAM") 
     } else {
        inCAAM = 0d       
     }

     // Test Share
     if (mi.in.get("CASA") != null) {
        inCASA = mi.in.get("CASA") 
     } else {
        inCASA = 0d       
     }

     // Level
     if (mi.in.get("PLVL") != null) {
        inPLVL = mi.in.get("PLVL") 
     } else {
        inPLVL = 1       
     }

     // Sub-Level
     if (mi.in.get("SLVL") != null) {
        inSLVL = mi.in.get("SLVL") 
     } else {
        inSLVL = 1       
     }
     
     // Parent Payee ID
     if (mi.in.get("PPID") != null) {
        inPPID = mi.in.get("PPID") 
     } else {
        inPPID = 0       
     }   

     // Is Alternate Hauler
     if (mi.in.get("ISAH") != null) {
        inISAH = mi.in.get("ISAH") 
     } else {
        inISAH = 0       
     }  
     
     // Truck
     if (mi.in.get("TRCK") != null) {
        inTRCK = mi.in.get("TRCK") 
     } else {
        inTRCK = ""         
     }


     logger.info("CONO ${CONO}")
     logger.info("DIVI ${DIVI}")
     logger.info("inRVID ${inRVID}")
     logger.info("inCASN ${inCASN}")
     logger.info("inCF15 ${inCF15}")
     
     // Validate contract payee record
     Optional<DBContainer> EXTCTP = findEXTCTP(CONO, DIVI, inRVID, inCPID)
     if(!EXTCTP.isPresent()){
        mi.error("Contract Payee doesn't exist")   
        return             
     }     
    
     // Update record
     updEXTCTPRecord()
     
  }
  
    
  //******************************************************************** 
  // Get EXTCTP record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTP(int CONO, String DIVI, String RVID, int CPID){  
     DBAction query = database.table("EXTCTP").index("00").build()
     def EXTCTP = query.getContainer()
     EXTCTP.set("EXCONO", CONO)
     EXTCTP.set("EXDIVI", DIVI)
     EXTCTP.set("EXRVID", RVID)
     EXTCTP.set("EXCPID", CPID)
     
     //logger.info("CONO ${CONO}")
     //logger.info("DIVI ${DIVI}")
     //logger.info("inRVID ${RVID}")
     //logger.info("inCASN ${CASN}")
     //logger.info("inCF15 ${CF15}")

     if(query.read(EXTCTP))  { 
       return Optional.of(EXTCTP)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Update EXTCTP record
  //********************************************************************    
  void updEXTCTPRecord(){ 
     
     DBAction action = database.table("EXTCTP").index("00").build()
     DBContainer EXTCTP = action.getContainer()
     
     EXTCTP.set("EXCONO", CONO)
     EXTCTP.set("EXDIVI", DIVI)
     EXTCTP.set("EXRVID", inRVID)
     EXTCTP.set("EXCPID", inCPID)

     // Read with lock
     action.readLock(EXTCTP, updateCallBackEXTCTP)
     }
   
     Closure<?> updateCallBackEXTCTP = { LockedResult lockedResult -> 

     if (inCASN != "") {
        lockedResult.set("EXCASN", inCASN)
     }
     
     if (inCF15 != 0) {
        lockedResult.set("EXCF15", inCF15)
     }
     
     if (inPYNM != "") {
        lockedResult.set("EXSUNM", inPYNM)
     }

     if (inSHTP != "") {
        lockedResult.set("EXSHTP", inSHTP)
     }

     if (inCATF != "") {
        lockedResult.set("EXCATF", inCATF)
     }

     if (inTFNM != "") {
        lockedResult.set("EXTFNM", inTFNM)
     }

     if (inCATP != 0) {
        lockedResult.set("EXCATP", inCATP)
     }

     if (inCAAM != 0) {
        lockedResult.set("EXCAAM", inCAAM)
     }

     if (inCASA != 0) {
        lockedResult.set("EXCASA", inCASA)
     }
     
     if (inPLVL != 0) {
        lockedResult.set("EXPLVL", inPLVL)
     }

     if (inSLVL != 0) {
        lockedResult.set("EXSLVL", inSLVL)
     }

     if (inPPID != 0) {
        lockedResult.set("EXPPID", inPPID)
     }

     if (inISAH != 0) {
        lockedResult.set("EXISAH", inISAH)
     }
     
     if (inTRCK != "") {
        lockedResult.set("EXTRCK", inTRCK)
     }


      // Get todays date
     LocalDateTime now = LocalDateTime.now();    
     DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");  
     String formatDate = now.format(format1);    
     
     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     
     // Update changed information
     int changeddate=Integer.parseInt(formatDate);   
     lockedResult.set("EXLMDT", changeddate)  
      
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }


} 

