// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete an contract instruction from EXTCTI
// Transaction DelContrInst
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * 
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: INIC - Instruction Code 
 * @param: DPOR - Display Order
 * 
*/


 public class DelContrInst extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelContrInst(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database 
       this.program = program
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
       String inRVID      
       if (mi.in.get("RVID") != null) {
          inRVID = mi.in.get("RVID") 
       } else {
          inRVID = ""     
       }
  
       // Instruction Code
       String inINIC      
       if (mi.in.get("INIC") != null) {
          inINIC = mi.in.get("INIC") 
       } else {
          inINIC = ""     
       }
  
       // Display Order
       int inDPOR     
       if (mi.in.get("DPOR") != null) {
          inDPOR = mi.in.get("DPOR") 
       } else {
          inDPOR = 0     
       }
  
       // Validate instruction record
       Optional<DBContainer> EXTCTI = findEXTCTI(inCONO, inDIVI, inRVID, inINIC, inDPOR)
       if(!EXTCTI.isPresent()){
          mi.error("Instruction Code doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCTIRecord(inCONO, inDIVI, inRVID, inINIC, inDPOR) 
       } 
       
    }
   
  
    //******************************************************************** 
    // Get EXTCTI record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCTI(int CONO, String DIVI, String RVID, String INIC, int DPOR){  
       DBAction query = database.table("EXTCTI").index("00").build()
       DBContainer EXTCTI = query.getContainer()
       EXTCTI.set("EXCONO", CONO)
       EXTCTI.set("EXDIVI", DIVI)
       EXTCTI.set("EXRVID", RVID)
       EXTCTI.set("EXINIC", INIC)
       EXTCTI.set("EXDPOR", DPOR)
       
       if(query.read(EXTCTI))  { 
         return Optional.of(EXTCTI)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTCTI
    //******************************************************************** 
    void deleteEXTCTIRecord(int CONO, String DIVI, String RVID, String INIC, int DPOR){ 
       DBAction action = database.table("EXTCTI").index("00").build()
       DBContainer EXTCTI = action.getContainer()
       EXTCTI.set("EXCONO", CONO)
       EXTCTI.set("EXDIVI", DIVI)
       EXTCTI.set("EXRVID", RVID)
       EXTCTI.set("EXINIC", INIC)
       EXTCTI.set("EXDPOR", DPOR)
  
       action.readLock(EXTCTI, deleterCallbackEXTCTI)
    }
      
    Closure<?> deleterCallbackEXTCTI = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }