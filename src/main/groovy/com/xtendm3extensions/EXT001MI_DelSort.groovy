// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a sort code from EXTSOR
// Transaction DelSort
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SORT - Sort Code
 * @param: ITNO - Item Number
 * 
*/


 public class DelSort extends ExtendM3Transaction {
    private final MIAPI mi  
    private final DatabaseAPI database  
    private final ProgramAPI program 
    private final LoggerAPI logger 
  
    int inCONO
    String inSORT
    String inITNO

    // Constructor 
    public DelSort(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi 
       this.database = database  
       this.program = program 
       this.logger = logger 
    } 
      
    public void main() { 
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Sort Code
       if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
          inSORT = mi.inData.get("SORT").trim() 
       } else {
          inSORT = ""     
       }
  
  
       // Validate sort code record
       Optional<DBContainer> EXTSOR = findEXTSOR(inCONO, inSORT)
       if(!EXTSOR.isPresent()){
          mi.error("Sort Code doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTSORRecord() 
       } 
       
    }
  
  
    //******************************************************************** 
    // Get EXTSOR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTSOR(int CONO, String SORT){  
       DBAction query = database.table("EXTSOR").index("00").build()
       DBContainer EXTSOR = query.getContainer()
       EXTSOR.set("EXCONO", CONO)
       EXTSOR.set("EXSORT", SORT)
       if(query.read(EXTSOR))  { 
         return Optional.of(EXTSOR)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTSOR
    //******************************************************************** 
    void deleteEXTSORRecord(){ 
       DBAction action = database.table("EXTSOR").index("00").build()
       DBContainer EXTSOR = action.getContainer()
       EXTSOR.set("EXCONO", inCONO) 
       EXTSOR.set("EXSORT", inSORT)
       action.readLock(EXTSOR, deleterCallbackEXTSOR)
    }
      
    Closure<?> deleterCallbackEXTSOR = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }