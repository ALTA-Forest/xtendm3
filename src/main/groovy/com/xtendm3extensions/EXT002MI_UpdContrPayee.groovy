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



public class UpdContrPayee extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
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
  public UpdContrPayee(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.in.get("DIVI")
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Revision ID
     if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
        inRVID = mi.inData.get("RVID").trim() 
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
     if (mi.in.get("CASN") != null && mi.in.get("CASN") != "") {
        inCASN = mi.inData.get("CASN").trim() 
        
        // Validate payee if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inCASN)
        if (!CIDMAS.isPresent()) {
           mi.error("Payee doesn't exist")   
           return             
        }

     } else {
        inCASN = ""         
     }
    
     // Payee Name
     if (mi.in.get("PYNM") != null && mi.in.get("PYNM") != "") {
        inPYNM = mi.inData.get("PYNM").trim() 
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
     if (mi.in.get("CATF") != null && mi.in.get("CATF") != "") {
        inCATF = mi.inData.get("CATF").trim() 
     } else {
        inCATF = ""         
     }    

     // Take From Name
     if (mi.in.get("TFNM") != null && mi.in.get("TFNM") != "") {
        inTFNM = mi.inData.get("TFNM").trim() 
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
     if (mi.in.get("TRCK") != null && mi.in.get("TRCK") != "") {
        inTRCK = mi.inData.get("TRCK").trim() 
     } else {
        inTRCK = ""         
     }

     
     // Validate contract payee record
     Optional<DBContainer> EXTCTP = findEXTCTP(inCONO, inDIVI, inRVID, inCPID)
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
     DBContainer EXTCTP = query.getContainer()
     EXTCTP.set("EXCONO", CONO)
     EXTCTP.set("EXDIVI", DIVI)
     EXTCTP.set("EXRVID", RVID)
     EXTCTP.set("EXCPID", CPID)

     if(query.read(EXTCTP))  { 
       return Optional.of(EXTCTP)
     } 
  
     return Optional.empty()
  }
  

   //******************************************************************** 
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }


  //******************************************************************** 
  // Update EXTCTP record
  //********************************************************************    
  void updEXTCTPRecord(){      
     DBAction action = database.table("EXTCTP").index("00").build()
     DBContainer EXTCTP = action.getContainer()     
     EXTCTP.set("EXCONO", inCONO)
     EXTCTP.set("EXDIVI", inDIVI)
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

       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
  }

} 

