// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update a contract instruction in EXTCTI
// Transaction UpdContrInst
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: INIC - Instruction Code
 * @param: DPOR - Display Order
 * 
*/



public class UpdContrInst extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inRVID
  String inINIC
  int inDPOR
  
  // Constructor 
  public UpdContrInst(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     //Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }
      
     // Instruction Code
     if (mi.in.get("INIC") != null) {
        inINIC = mi.in.get("INIC") 
     } else {
        inINIC = ""        
     }
      
     // Display Order
     if (mi.in.get("DPOR") != null) {
        inDPOR = mi.in.get("DPOR") 
     } 


     // Validate Contract Instruction record
     Optional<DBContainer> EXTCTI = findEXTCTI(inCONO, inDIVI, inRVID, inINIC)
     if(!EXTCTI.isPresent()){
        mi.error("Contract Instruction doesn't exist")   
        return             
     }     
    
     // Update record
     updEXTCTIRecord()
     
  }
  
  //******************************************************************** 
  // Get EXTCTI record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTI(int CONO, String DIVI, String RVID, String INIC){  
     DBAction query = database.table("EXTCTI").index("00").build()
     def EXTCTI = query.getContainer()
     EXTCTI.set("EXCONO", CONO)
     EXTCTI.set("EXDIVI", DIVI)
     EXTCTI.set("EXRVID", RVID)
     EXTCTI.set("EXINIC", INIC)
     if(query.read(EXTCTI))  { 
       return Optional.of(EXTCTI)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Update EXTCTI record
  //********************************************************************    
  void updEXTCTIRecord(){      
     DBAction action = database.table("EXTCTI").index("00").build()
     DBContainer EXTCTI = action.getContainer()    
     EXTCTI.set("EXCONO", inCONO)
     EXTCTI.set("EXDIVI", inDIVI)
     EXTCTI.set("EXRVID", inRVID)
     EXTCTI.set("EXINIC", inINIC)

     // Read with lock
     action.readLock(EXTCTI, updateCallBackEXTCTI)
     }
   
     Closure<?> updateCallBackEXTCTI = { LockedResult lockedResult -> 
       if (mi.in.get("DPOR") != null) {
          lockedResult.set("EXDPOR", mi.in.get("DPOR"))
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

