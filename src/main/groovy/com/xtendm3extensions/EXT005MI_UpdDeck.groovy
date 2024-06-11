// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update deck header in EXTDPH
// Transaction UpdDeck
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: DPNA - Deck Name
 * @param: TYPE - Deck Type
 * @param: SORT - Sort Code
 * @param: MBFW - Weight of 1 MBF
 * @param: DPDT - Deck Date
 * @param: DPLC - Life Cycle
 * 
*/



public class UpdDeck extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inDPNA
  int inDPID
  String inTYPE
  String inSORT
  String inYARD
  double inMBFW
  int inDPDT
  int inDPLC
  
  // Constructor 
  public UpdDeck(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0        
     }

     // Deck Name
     if (mi.in.get("DPNA") != null) {
        inDPNA = mi.in.get("DPNA") 
     } else {
        inDPNA = ""         
     }

     // Deck Type
     if (mi.in.get("TYPE") != null) {
        inTYPE = mi.in.get("TYPE") 
     } else {
        inTYPE = ""         
     }
           
     // Sort Code
     if (mi.in.get("SORT") != null) {
        inSORT = mi.in.get("SORT") 
     } else {
        inSORT= ""      
     }
     
     // Yard
     if (mi.in.get("YARD") != null) {
        inYARD = mi.in.get("YARD") 
     } else {
        inYARD = ""      
     }

     // Weight of 1 MBF
     if (mi.in.get("MBFW") != null) {
        inMBFW = mi.in.get("MBFW") 
     } 

     // Deck Date
     if (mi.in.get("DPDT") != null) {
        inDPDT = mi.in.get("DPDT") 
        
        //Validate date format
        boolean validDPDT = utility.call("DateUtil", "isDateValid", String.valueOf(inDPDT), "yyyyMMdd")  
        if (!validDPDT) {
           mi.error("Deck Date is not valid")   
           return  
        } 

     } 
     
     // Life Cycle
     if (mi.in.get("DPLC") != null) {
        inDPLC = mi.in.get("DPLC") 
     }


     // Validate Deck Head record
     Optional<DBContainer> EXTDPH = findEXTDPH(inCONO, inDIVI, inDPID)
     if (!EXTDPH.isPresent()) {
        mi.error("Weight Ticket Line doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDPHRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDPH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPH(int CONO, String DIVI, int DPID){  
     DBAction query = database.table("EXTDPH").index("00").build()
     DBContainer EXTDPH = query.getContainer()
     EXTDPH.set("EXCONO", CONO)
     EXTDPH.set("EXDIVI", DIVI)
     EXTDPH.set("EXDPID", DPID)
     if(query.read(EXTDPH))  { 
       return Optional.of(EXTDPH)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTDPH record
  //********************************************************************    
  void updEXTDPHRecord(){      
     DBAction action = database.table("EXTDPH").index("00").build()
     DBContainer EXTDPH = action.getContainer()     
     EXTDPH.set("EXCONO", inCONO)
     EXTDPH.set("EXDIVI", inDIVI)
     EXTDPH.set("EXDPID", inDPID)

     // Read with lock
     action.readLock(EXTDPH, updateCallBackEXTDPH)
     }
   
     Closure<?> updateCallBackEXTDPH = { LockedResult lockedResult -> 
       if (inDPNA != null && inDPNA != "") {
          lockedResult.set("EXDPNA", inDPNA)
       }
  
       if (inSORT != null && inSORT != "") {
          lockedResult.set("EXSORT", inSORT)
       }
       
       if (inYARD != null && inYARD != "") {
          lockedResult.set("EXYARD", inYARD)
       }
     
       if (inTYPE != null && inTYPE != "") {
          lockedResult.set("EXTYPE", inTYPE)
       }
    
       if (inMBFW != null) {
          lockedResult.set("EXMBFW", inMBFW)
       }
  
       if (inDPDT != null) {
          lockedResult.set("EXDPDT", inDPDT)
       }
   
       if (inDPLC != null) {
          lockedResult.set("EXDPLC", inDPLC)
       }
      
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)          
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

