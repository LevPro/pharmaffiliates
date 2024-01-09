package org.levpro.pharmaffiliates;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.levpro.pharmaffiliates.models.Categories;
import org.levpro.pharmaffiliates.models.Childrens;
import org.levpro.pharmaffiliates.models.Products;
import org.levpro.pharmaffiliates.utils.PageLoader;

public class Main {
    public static void main(String[] args) throws IOException {
        File resultDir = new File(System.getProperty("user.dir"), "result");

        if (!resultDir.exists()) {
            resultDir.mkdir();
        }

        System.out.println("Get parsed links data file");

        ArrayList<String> parsedLinks = new ArrayList<>();

        File parsedLinksJsonFile = new File(resultDir, "parsed_links.json");

        if (parsedLinksJsonFile.exists()) {
            BufferedReader parsedProductsLinksBuffer = new BufferedReader(new FileReader(parsedLinksJsonFile));

            StringBuilder parsedProductsLinksStringBuffer = new StringBuilder();

            String parsedLinksLine;

            while ((parsedLinksLine = parsedProductsLinksBuffer.readLine()) != null) {
                parsedProductsLinksStringBuffer.append(parsedLinksLine);
            }

            parsedProductsLinksBuffer.close();

            String parsedProductsLinksJsonString = parsedProductsLinksStringBuffer.toString();

            parsedLinks = new Gson().fromJson(parsedProductsLinksJsonString, parsedLinks.getClass());
        }

        System.out.println("Get categories data file");

        ArrayList<Categories> categoriesResult = new ArrayList<>();

        File categoriesResultJsonFile = new File(resultDir, "categories.json");

        if (categoriesResultJsonFile.exists()) {
            BufferedReader categoriesResultBuffer = new BufferedReader(new FileReader(categoriesResultJsonFile));

            StringBuilder categoriesResultStringBuffer = new StringBuilder();

            String categoriesResultLine;

            while ((categoriesResultLine = categoriesResultBuffer.readLine()) != null) {
                categoriesResultStringBuffer.append(categoriesResultLine);
            }

            categoriesResultBuffer.close();

            String categoriesResultJsonString = categoriesResultStringBuffer.toString();

            categoriesResult = new Gson().fromJson(categoriesResultJsonString, categoriesResult.getClass());
        };

        System.out.println("Get products data file");

        ArrayList<Products> productsResult = new ArrayList<>();

        File productsResultJsonFile = new File(resultDir, "products.json");

        if (productsResultJsonFile.exists()) {
            BufferedReader productsResultBuffer = new BufferedReader(new FileReader(productsResultJsonFile));

            StringBuilder productsResultStringBuffer = new StringBuilder();

            String productsResultLine;

            while ((productsResultLine = productsResultBuffer.readLine()) != null) {
                productsResultStringBuffer.append(productsResultLine);
            }

            productsResultBuffer.close();

            String productsResultJsonString = productsResultStringBuffer.toString();

            productsResult = new Gson().fromJson(productsResultJsonString, productsResult.getClass());
        };

        System.out.println("Get childrens data file");

        ArrayList<Childrens> childrens = new ArrayList<>();

        File childrensJsonFile = new File(resultDir, "childrens.json");

        if (childrensJsonFile.exists()) {
            BufferedReader childrensBuffer = new BufferedReader(new FileReader(childrensJsonFile));

            StringBuilder parsedProductsLinksStringBuffer = new StringBuilder();

            String childrensLine;

            while ((childrensLine = childrensBuffer.readLine()) != null) {
                parsedProductsLinksStringBuffer.append(childrensLine);
            }

            childrensBuffer.close();

            String childrensJsonString = parsedProductsLinksStringBuffer.toString();

            childrens = new Gson().fromJson(childrensJsonString, childrens.getClass());
        }

        System.out.println("Load input links from file");

        File inputDataFile = new File(System.getProperty("user.dir"), "data.json");

        BufferedReader inputDataBuffer = new BufferedReader(new FileReader(inputDataFile));

        StringBuilder inputDataStringBuffer = new StringBuilder();

        String inputDataLine;

        while ((inputDataLine = inputDataBuffer.readLine()) != null) {
            inputDataStringBuffer.append(inputDataLine);
        }

        inputDataBuffer.close();

        String inputDataJsonString = inputDataStringBuffer.toString();

        Map<String, Object> inputData = new HashMap<>();

        inputData = new Gson().fromJson(inputDataJsonString, inputData.getClass());

        for (Map<String, Object> category : (ArrayList<Map<String, Object>>) inputData.get("categories")) {
            if (parsedLinks.contains((String) category.get("url"))) {
                System.out.println(String.format("%s parse early", category.get("url")));

                continue;
            }

            System.out.println("Add to collection");

            categoriesResult.add(new Categories((String) category.get("url"), (String) category.get("name"), "", ""));

            System.out.println(String.format("Load %s", category.get("url")));

            String categoryOutput;

            try {
                categoryOutput = PageLoader.load((String) category.get("url"), "GET");
            } catch (Exception exception) {
                System.out.println(String.format("Load error %s", exception));

                continue;
            }

            Document categoryDocument = Jsoup.parse(categoryOutput, "", Parser.xmlParser());

            System.out.println("Get childrens");

            Elements childrensLinks = categoryDocument.select(".alpahbatic-container .cat-box");

            for (Element childrenLink : childrensLinks) {
                String childrenLinkUrl = childrenLink.select("a").get(0).attr("href").replace(" ", "%20");
                String childrenLinkName = childrenLink.select("a").get(0).text();

                System.out.println(String.format("Add children %s - %s", childrenLinkUrl, childrenLinkName));

                childrens.add(new Childrens(childrenLinkUrl, childrenLinkName));
            }

            System.out.println("Get products");

            Elements productsLinks = categoryDocument.select(".main-page .productbox");

            for (Element productLink : productsLinks) {
                String productLinkUrl = productLink.select(".product-details .btn-hover").get(0).attr("href").replace(" ", "%20");
                String productLinkName = productLink.select(".prodcut-title-name h2").get(0).text().replace("\r\n", " ").trim();
                String productLinkImage = productLink.select(".image-container img").get(0).attr("src");

                System.out.println(String.format("Add products url %s - %s", productLinkUrl, productLinkName));

                productsResult.add(new Products(productLinkUrl, (String) category.get("name"), productLinkName, productLinkImage));
            }

            System.out.println("Save products");

            FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
            productsFileWriter.write(new Gson().toJson(productsResult));
            productsFileWriter.close();

            System.out.println("Save categories");

            FileWriter categoresFileWriter = new FileWriter(categoriesResultJsonFile);
            categoresFileWriter.write(new Gson().toJson(categoriesResult));
            categoresFileWriter.close();

            System.out.println("Save childrens");

            FileWriter childrensFileWriter = new FileWriter(childrensJsonFile);
            childrensFileWriter.write(new Gson().toJson(childrens));
            childrensFileWriter.close();

            System.out.println("Save parsed links");

            parsedLinks.add((String) category.get("url"));

            FileWriter parsedProductsLinksFileWriter = new FileWriter(parsedLinksJsonFile);
            parsedProductsLinksFileWriter.write(new Gson().toJson(parsedLinks));
            parsedProductsLinksFileWriter.close();
        }

        while (childrens.size() > 0) {
            if (parsedLinks.contains(childrens.get(0).getUrl())) {
                System.out.println(String.format("%s parse early", childrens.get(0).getUrl()));

                childrens.remove(0);

                continue;
            }

            System.out.println(String.format("Load %s", childrens.get(0).getUrl()));

            String childrenOutput;

            try {
                childrenOutput = PageLoader.load(childrens.get(0).getUrl(), "GET");
            } catch (Exception exception) {
                System.out.println(String.format("Load error %s", exception));

                childrens.remove(0);

                continue;
            }

            Document childrenDocument = Jsoup.parse(childrenOutput, "", Parser.xmlParser());

            System.out.println("Get childrens");

            Elements childrensChildrensLinks = childrenDocument.select(".alpahbatic-container .cat-box");

            for (Element childrenChildrenLink : childrensChildrensLinks) {
                String childrenChildrenUrl = childrenChildrenLink.select("a").get(0).attr("href").replace(" ", "%20");
                String childrenChildrenName = childrenChildrenLink.select("a").get(0).text();

                System.out.println(String.format("Add children %s - %s", childrenChildrenUrl, childrenChildrenName));

                childrens.add(new Childrens(childrenChildrenUrl, childrenChildrenName));
            }

            Elements childrenNameElement = childrenDocument.select(".s1 h1");

            if (childrenNameElement.isEmpty()) {
                childrenNameElement = childrenDocument.select(".main-page .page-title h1");
            }

            if (childrenNameElement.isEmpty()) {
                System.out.println("Name not found");

                childrens.remove(0);

                continue;
            }

            String childrenName = childrenNameElement.get(0).text().replace("\r\n", " ").trim();

            Elements childrenDescriptionElement = childrenDocument.select(".s1 .clearheader");

            if (childrenDescriptionElement.isEmpty()) {
                childrenDescriptionElement = childrenDocument.select(".s1 .clearheader");
            }

            String childrenDescription = "";

            if (!childrenDescriptionElement.isEmpty()) {
                childrenDescription = childrenDescriptionElement.get(0).text().replace("\r\n", " ").trim();
            }

            System.out.println("Add to collection");

            categoriesResult.add(new Categories(childrens.get(0).getUrl(), childrenName, childrenDescription, childrens.get(0).getName()));

            System.out.println("Get products");

            Elements childrenProductsLinks = childrenDocument.select(".main-page .productbox");

            for (Element childrenProductsLink : childrenProductsLinks) {
                String childrenProductsLinkUrl = childrenProductsLink.select(".product-details .btn-hover").get(0).attr("href").replace(" ", "%20");
                String childrenProductsLinkName = childrenProductsLink.select(".prodcut-title-name h2").get(0).text().replace("\r\n", " ").trim();
                String childrenProductsLinkImage = childrenProductsLink.select(".image-container img").get(0).attr("src");

                System.out.println(String.format("Add products url %s - %s", childrenProductsLinkUrl, childrenProductsLinkName));

                productsResult.add(new Products(childrenProductsLinkUrl, childrenName, childrenProductsLinkName, childrenProductsLinkImage));
            }

            System.out.println("Save products");

            FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
            productsFileWriter.write(new Gson().toJson(productsResult));
            productsFileWriter.close();

            System.out.println("Save categories");

            FileWriter categoresFileWriter = new FileWriter(categoriesResultJsonFile);
            categoresFileWriter.write(new Gson().toJson(categoriesResult));
            categoresFileWriter.close();

            System.out.println("Save childrens");

            FileWriter childrensFileWriter = new FileWriter(childrensJsonFile);
            childrensFileWriter.write(new Gson().toJson(childrens));
            childrensFileWriter.close();

            System.out.println("Save parsed links");

            parsedLinks.add(childrens.get(0).getUrl());

            FileWriter parsedProductsLinksFileWriter = new FileWriter(parsedLinksJsonFile);
            parsedProductsLinksFileWriter.write(new Gson().toJson(parsedLinks));
            parsedProductsLinksFileWriter.close();

            childrens.remove(0);
        }

        System.out.println(String.format("Found %s product and %s categories", productsResult.size(), categoriesResult.size()));

        categoriesResult.clear();
        childrens.clear();

        for (int index = 0; index < productsResult.size(); index++) {
            if (parsedLinks.contains(productsResult.get(index).getUrl())) {
                System.out.println(String.format("%s parsed early", productsResult.get(index).getUrl()));

                continue;
            }

            System.out.println(String.format("Load %s", productsResult.get(index).getUrl()));

            String productOutput;

            try {
                productOutput = PageLoader.load(productsResult.get(index).getUrl(), "GET");
            } catch (Exception exception) {
                System.out.println(String.format("Load error %s", exception));

                continue;
            }

            Document productDocument = Jsoup.parse(productOutput, "", Parser.xmlParser());

            Elements productNameElement = productDocument.select(".title-product h1");

            if (!productNameElement.isEmpty()) {
                productsResult.get(index).setName(productNameElement.get(0).text().replace("\r\n", " ").trim());
            }

            Elements productDescriptionElement = productDocument.select(".single-product .productpage-disclaimer");

            if (!productDescriptionElement.isEmpty()) {
                productsResult.get(index).setDescription(productDescriptionElement.get(0).html().replace("\r\n", " ").trim());
            }

            Elements productSearchKeywordsElement = productDocument.select(".single-product .keywords-container .keywordtext");

            if (!productSearchKeywordsElement.isEmpty()) {
                productsResult.get(index).setSearchKeywords(productSearchKeywordsElement.get(0).html().replace("\r\n", " ").trim());
            }

            Elements productImageElement = productDocument.select(".single-product .product_detail_right .images a");

            if (!productImageElement.isEmpty()) {
                productsResult.get(index).setImage(productImageElement.get(0).attr("href"));
            }

            Elements productParams = productDocument.select(".single-product .product_detail_left_in .shop_table tr");

            for (Element productParam: productParams) {
                productsResult.get(index).addParam(productParam.select("td").get(0).text().replace("\r\n", " ").trim(), productParam.select("td").get(1).text().replace("\r\n", " ").trim());
            }

            System.out.println("Save products");

            FileWriter productsFileWriter = new FileWriter(productsResultJsonFile);
            productsFileWriter.write(new Gson().toJson(productsResult));
            productsFileWriter.close();

            System.out.println("Save parsed links");

            parsedLinks.add(productsResult.get(index).getUrl());

            FileWriter parsedProductsLinksFileWriter = new FileWriter(parsedLinksJsonFile);
            parsedProductsLinksFileWriter.write(new Gson().toJson(parsedLinks));
            parsedProductsLinksFileWriter.close();
        }

        System.out.println("Program complete");
    }
}
