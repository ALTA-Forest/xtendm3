// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract details from EXTCTD 
// Transaction DelContractDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * 
*/


 public class DelContractDet extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final MICallerAPI miCaller
    private final LoggerAPI logger
  
    Integer inCONO
    String inDIVI
    String inRVID 
    int inCTNO
    String inBRND
    String inINIC
    String inCASN
    int inCF15
    int inSEQN

  // Constructor 
  public DelContractDet(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
     this.miCaller = miCaller
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
     
     // Validate contract revision
     Optional<DBContainer> EXTCTD = findEXTCTD(inCONO, inDIVI, inRVID)
     if(!EXTCTD.isPresent()){
        mi.error("Contract Revision doesn't exist")   
        return             
     } else {
        //Get contract Number for the Revision
        DBContainer containerEXTCTD = EXTCTD.get() 
        inCTNO = containerEXTCTD.get("EXCTNO")

        // Delete records 
        deleteEXTCTDRecord() 
        deleteEXTCTBRecord() 
        deleteEXTCTIRecord()
        deleteEXTCTPRecord()
        deleteEXTCTSRecord()
        deleteEXTCTTRecord()
        
       //Delete for each Section
       List<DBContainer> resultEXTCDS = listEXTCDS(inCONO, inDIVI, inRVID) 
       for (DBContainer recLineEXTCDS : resultEXTCDS){ 
          String revisionIDString = recLineEXTCDS.get("EXRVID")
          String sectionIDString = recLineEXTCDS.get("EXDSID")
          int sectionID = recLineEXTCDS.get("EXDSID")
          String sectionNumberString = recLineEXTCDS.get("EXCSSN")
          int sectionNumber = recLineEXTCDS.get("EXCSSN")
          String sectionNameString = recLineEXTCDS.get("EXCSNA")
          String displayOrderString = recLineEXTCDS.get("EXDPOR")
          String speciesString = recLineEXTCDS.get("EXCSSP")
          String sectionGradeString = recLineEXTCDS.get("EXCSGR")
          String sectionExceptionString = recLineEXTCDS.get("EXCSEX")
          String lengthIncrementString = recLineEXTCDS.get("EXCSLI")
  
          //Delete Species for each section
          List<DBContainer> resultEXTCSS = listEXTCSS(inCONO, inDIVI, sectionID) 
          for (DBContainer recLineEXTCSS : resultEXTCSS){ 
            String sectionIDSpeciesString = recLineEXTCSS.get("EXDSID")
            String speciesNameString = recLineEXTCSS.get("EXSPEC")

            deleteSectionSpecMI(String.valueOf(inCONO), inDIVI, sectionIDString, speciesNameString)   
          }
  
          //Delete Sections Grades for each section
          List<DBContainer> resultEXTCSG = listEXTCSG(inCONO, inDIVI, sectionID) 
          for (DBContainer recLineEXTCSG : resultEXTCSG){ 
            String sectionIDGradesString = recLineEXTCSG.get("EXDSID")
            String gradeCodeString = recLineEXTCSG.get("EXGRAD")

            deleteSectionGradeMI(String.valueOf(inCONO), inDIVI, sectionIDString, gradeCodeString)   
          }
   
          //Delete Sections Exceptions for each section
          List<DBContainer> ResultEXTCSE = listEXTCSE(inCONO, inDIVI, sectionID) 
          for (DBContainer recLineEXTCSE : ResultEXTCSE){ 
            String sectionIDExceptionString = recLineEXTCSE.get("EXDSID")
            String exceptionCodeString = recLineEXTCSE.get("EXECOD")

            deleteSectionExeptionMI(String.valueOf(inCONO), inDIVI, sectionIDString, exceptionCodeString)   
          }

          //Delete Sections Rate for each section
          List<DBContainer> resultEXTCSR = listEXTCSR(inCONO, inDIVI, sectionID) 
          for (DBContainer recLineEXTCSR : resultEXTCSR){ 
            String sectionIDRateString = recLineEXTCSR.get("EXDSID")
            String rateSeqString = recLineEXTCSR.get("EXCRSQ")

            deleteSectionRateMI(String.valueOf(inCONO), inDIVI, sectionIDString, rateSeqString)   
          }
          
       }

        deleteEXTCDSRecord()
     }

  }
 

  //******************************************************************** 
  // Get EXTCTD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTD(int CONO, String DIVI, String RVID){  
     DBAction query = database.table("EXTCTD").index("00").selection("EXCTNO").build()
     DBContainer EXTCTD = query.getContainer()
     EXTCTD.set("EXCONO", CONO)
     EXTCTD.set("EXDIVI", DIVI)
     EXTCTD.set("EXRVID", RVID)
     if(query.read(EXTCTD))  { 
       return Optional.of(EXTCTD)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Read records from section table EXTCDS
  //********************************************************************  
  private List<DBContainer> listEXTCDS(int CONO, String DIVI, String RVID){
    List<DBContainer>recLineEXTCDS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCDS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCDS").index("00").matching(expression).selection("EXRVID", "EXDSID", "EXCSSN", "EXCSNA", "EXDPOR", "EXCSSP", "EXCSGR", "EXCSEX", "EXCSLI").build()
    DBContainer EXTCDS = query.createContainer()
    EXTCDS.set("EXCONO", CONO)
    EXTCDS.set("EXDIVI", DIVI)
    EXTCDS.set("EXRVID", RVID)
    
	  int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCDS, 3, pageSize, { DBContainer recordEXTCDS ->  
       recLineEXTCDS.add(recordEXTCDS.createCopy()) 
    })

    return recLineEXTCDS
  }


  //******************************************************************** 
  // Read records from species table EXTCSS
  //********************************************************************  
  private List<DBContainer> listEXTCSS(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSS = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSS")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSS").index("00").matching(expression).selection("EXDSID", "EXSPEC").build()
    DBContainer EXTCSS = query.createContainer()
    EXTCSS.set("EXCONO", CONO)
    EXTCSS.set("EXDIVI", DIVI)
    EXTCSS.set("EXDSID", DSID)
    
	  int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSS, 3, pageSize, { DBContainer recordEXTCSS ->  
       recLineEXTCSS.add(recordEXTCSS.createCopy()) 
    })

    return recLineEXTCSS
  }


  //******************************************************************** 
  // Read records from section grade table EXTCSG
  //********************************************************************  
  private List<DBContainer> listEXTCSG(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSG = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSG")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSG").index("00").matching(expression).selection("EXDSID", "EXGRAD").build()
    DBContainer EXTCSG = query.createContainer()
    EXTCSG.set("EXCONO", CONO)
    EXTCSG.set("EXDIVI", DIVI)
    EXTCSG.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSG, 3, pageSize, { DBContainer recordEXTCSG ->  
       recLineEXTCSG.add(recordEXTCSG.createCopy()) 
    })

    return recLineEXTCSG
  }

  //******************************************************************** 
  // Read records from section exception table EXTCSE
  //********************************************************************  
  private List<DBContainer> listEXTCSE(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSE = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSE")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSE").index("00").matching(expression).selection("EXDSID", "EXECOD").build()
    DBContainer EXTCSE = query.createContainer()
    EXTCSE.set("EXCONO", CONO)
    EXTCSE.set("EXDIVI", DIVI)
    EXTCSE.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSE, 3, pageSize, { DBContainer recordEXTCSE ->  
       recLineEXTCSE.add(recordEXTCSE.createCopy()) 
    })

    return recLineEXTCSE
  }

  //******************************************************************** 
  // Read records from section rate table EXTCSR
  //********************************************************************  
  private List<DBContainer> listEXTCSR(int CONO, String DIVI, int DSID){
    List<DBContainer>recLineEXTCSR = new ArrayList() 
    ExpressionFactory expression = database.getExpressionFactory("EXTCSR")
    expression = expression.eq("EXCONO", String.valueOf(CONO))
    
    DBAction query = database.table("EXTCSR").index("00").matching(expression).selection("EXDSID", "EXCRSQ").build()
    DBContainer EXTCSR = query.createContainer()
    EXTCSR.set("EXCONO", CONO)
    EXTCSR.set("EXDIVI", DIVI)
    EXTCSR.set("EXDSID", DSID)

    int pageSize = mi.getMaxRecords() <= 0 || mi.getMaxRecords() >= 10000? 10000: mi.getMaxRecords()        
    query.readAll(EXTCSR, 3, pageSize, { DBContainer recordEXTCSR ->  
       recLineEXTCSR.add(recordEXTCSR.createCopy()) 
    })

    return recLineEXTCSR
  }



  //******************************************************************** 
  // Delete record in EXTCTD
  //******************************************************************** 
  void deleteEXTCTDRecord(){ 
     DBAction action = database.table("EXTCTD").index("00").build()
     DBContainer EXTCTD = action.getContainer()    
     EXTCTD.set("EXCONO", inCONO) 
     EXTCTD.set("EXDIVI", inDIVI) 
     EXTCTD.set("EXRVID", inRVID)
     
     action.readAllLock(EXTCTD, 3, deleterCallbackEXTCTD)
  }
    
  Closure<?> deleterCallbackEXTCTD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record in EXTCTB - Contract Brand
  //******************************************************************** 
  void deleteEXTCTBRecord(){ 
     DBAction action = database.table("EXTCTB").index("00").build()
     DBContainer EXTCTB = action.getContainer()    
     EXTCTB.set("EXCONO", inCONO) 
     EXTCTB.set("EXDIVI", inDIVI) 
     EXTCTB.set("EXRVID", inRVID)

     action.readAllLock(EXTCTB, 3, deleterCallbackEXTCTB)
  }
    
  Closure<?> deleterCallbackEXTCTB = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record in EXTCTI - Contract Instruction
  //******************************************************************** 
  void deleteEXTCTIRecord(){ 
     DBAction action = database.table("EXTCTI").index("00").build()
     DBContainer EXTCTI = action.getContainer()
     EXTCTI.set("EXCONO", inCONO) 
     EXTCTI.set("EXDIVI", inDIVI) 
     EXTCTI.set("EXRVID", inRVID)
     
     action.readAllLock(EXTCTI, 3, deleterCallbackEXTCTI)
  }
    
  Closure<?> deleterCallbackEXTCTI = { LockedResult lockedResult ->  
     lockedResult.delete()
  }

  
  //******************************************************************** 
  // Delete record in EXTCTP - Contract Payee
  //******************************************************************** 
  void deleteEXTCTPRecord(){ 
     DBAction action = database.table("EXTCTP").index("00").build()
     DBContainer EXTCTP = action.getContainer()    
     EXTCTP.set("EXCONO", inCONO) 
     EXTCTP.set("EXDIVI", inDIVI) 
     EXTCTP.set("EXRVID", inRVID)

     action.readAllLock(EXTCTP, 3, deleterCallbackEXTCTP)
  }
    
  Closure<?> deleterCallbackEXTCTP = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record in EXTCTS - Contract Status
  //******************************************************************** 
  void deleteEXTCTSRecord(){ 
     DBAction action = database.table("EXTCTS").index("00").build()
     DBContainer EXTCTS = action.getContainer()    
     EXTCTS.set("EXCONO", inCONO) 
     EXTCTS.set("EXDIVI", inDIVI) 
     EXTCTS.set("EXRVID", inRVID)

     action.readAllLock(EXTCTS, 3, deleterCallbackEXTCTS)
  }
    
  Closure<?> deleterCallbackEXTCTS = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record in EXTCTT - Contract Trip
  //******************************************************************** 
  void deleteEXTCTTRecord(){ 
     DBAction action = database.table("EXTCTT").index("00").build()
     DBContainer EXTCTT = action.getContainer()    
     EXTCTT.set("EXCONO", inCONO) 
     EXTCTT.set("EXDIVI", inDIVI) 
     EXTCTT.set("EXCTNO", inCTNO)
     EXTCTT.set("EXRVID", inRVID)

     action.readAllLock(EXTCTT, 4, deleterCallbackEXTCTT)
  }
    
  Closure<?> deleterCallbackEXTCTT = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record in EXTCDS - Contract Sections
  //******************************************************************** 
  void deleteEXTCDSRecord(){ 
     DBAction action = database.table("EXTCDS").index("00").build()
     DBContainer EXTCDS = action.getContainer()   
     EXTCDS.set("EXCONO", inCONO) 
     EXTCDS.set("EXDIVI", inDIVI) 
     EXTCDS.set("EXRVID", inRVID)

     action.readAllLock(EXTCDS, 3, deleterCallbackEXTCDS)
  }
    
  Closure<?> deleterCallbackEXTCDS = { LockedResult lockedResult ->  
     lockedResult.delete()
  }



   //***************************************************************************** 
   // Delete Section Species
   //***************************************************************************** 
   void deleteSectionSpecMI(String company, String division, String sectionID, String speciesName){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, SPEC: speciesName] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.SSID != null){
          }
        }

        miCaller.call("EXT002MI","DelSectionSpec", params, callback)
   } 

   //***************************************************************************** 
   // Delete Section Grade
   //***************************************************************************** 
   void deleteSectionGradeMI(String company, String division, String sectionID, String gradeCode){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, GRAD: gradeCode] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","DelSectionGrade", params, callback)
   } 

   //***************************************************************************** 
   // Delete Section Exception
   //***************************************************************************** 
   void deleteSectionExeptionMI(String company, String division, String sectionID, String exceptionCode){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, ECOD: exceptionCode] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","DelSectionExcep", params, callback)
   } 

   //***************************************************************************** 
   // Delete Section Rate
   //***************************************************************************** 
   void deleteSectionRateMI(String company, String division, String sectionID, String rateSeq){   
        Map<String, String> params = [CONO: company, DIVI: division, DSID: sectionID, CRSQ: rateSeq] 
        Closure<?> callback = {
          Map<String, String> response ->
          if(response.DSID != null){
          }
        }

        miCaller.call("EXT002MI","DelSectionRate", params, callback)
   } 


 }