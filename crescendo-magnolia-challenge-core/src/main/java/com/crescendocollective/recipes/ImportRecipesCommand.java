package com.crescendocollective.recipes;

import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.dam.jcr.JcrAssetProvider;
import info.magnolia.dam.jcr.JcrFolder;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.Components;
import java.io.IOException;
import javax.jcr.RepositoryException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jackrabbit.commons.JcrUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.Session;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ThreadLocalRandom;

public class ImportRecipesCommand extends BaseRepositoryCommand {

    @Override
    public boolean execute(Context context) throws Exception {
        Session recipeSession = context.getJCRSession("website");
        Node rootNode = recipeSession.getRootNode();

        String recipesCaption = "recipes";
        Node recipes = rootNode.addNode(recipesCaption, "mgnl:page");
        NodeTypes.Renderable.set(recipes, "crescendo-magnolia-challenge:pages/recipes/recipes");
        PropertyUtil.setProperty(recipes, "title", "Recipes Title");
        recipeSession.save();

        AssetProviderRegistry assetProviderRegistry = Components.getComponent(AssetProviderRegistry.class);
        JcrAssetProvider jcrAssetProvider = (JcrAssetProvider) assetProviderRegistry.getProviderById(DamConstants.DEFAULT_JCR_PROVIDER_ID);
        JcrFolder assetFolder = (JcrFolder) jcrAssetProvider.getRootFolder();
        Node assetFolderNode = assetFolder.getNode();
        Session imgSession = MgnlContext.getJCRSession(DamConstants.WORKSPACE);

        JSONArray jsonArray = makeRequest(context);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            processObject(object, recipes, assetFolderNode, imgSession);
        }

        imgSession.save();
        recipeSession.save();
        return true;
    }

    private void processObject(JSONObject object, Node recipes, Node assetFolderNode, Session imgSession)
                throws RepositoryException, IOException {
        Node recipe = recipes.addNode(object.getString("title"), "mgnl:page");
        NodeTypes.Renderable.set(recipe, "crescendo-magnolia-challenge:pages/recipes/recipe");
        PropertyUtil.setProperty(recipe, "title", object.getString("title"));
        PropertyUtil.setProperty(recipe, "description", object.getString("description"));
        PropertyUtil.setProperty(recipe, "prepTime", object.getString("prepTime"));
        PropertyUtil.setProperty(recipe, "cookTime", object.getString("cookTime"));
        PropertyUtil.setProperty(recipe, "servingSize", object.getString("servingSize"));

        String imgUrl = "http:" + object.getString("largeImageUrl");
        String imgExtension = imgUrl.substring(imgUrl.lastIndexOf(".") + 1);
        String imgTitle = Integer.toString(ThreadLocalRandom.current().nextInt()) + imgUrl.substring(imgUrl.lastIndexOf("."));
        String mimeType = URLConnection.guessContentTypeFromName(imgTitle);

        URL url = new URL(imgUrl);
        long contentLength = url.openConnection().getContentLength();
        BufferedImage img = ImageIO.read(url);
        int height = img.getHeight();
        int width = img.getWidth();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, imgExtension, os);
        InputStream imageStream = new ByteArrayInputStream(os.toByteArray());

        Node assetNode = JcrUtils.getOrAddNode(assetFolderNode, imgTitle, AssetNodeTypes.Asset.NAME);
        assetNode.setProperty(AssetNodeTypes.Asset.ASSET_NAME, imgTitle);

        Node assetResourceNode = JcrUtils.getOrAddNode(assetNode, AssetNodeTypes.AssetResource.RESOURCE_NAME, AssetNodeTypes.AssetResource.NAME);
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.DATA, imgSession.getValueFactory().createBinary(imageStream));
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.FILENAME, imgTitle);
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.EXTENSION, imgTitle);
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.SIZE, Long.toString(contentLength));
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.MIMETYPE, mimeType);
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.WIDTH, Long.toString(width));
        assetResourceNode.setProperty(AssetNodeTypes.AssetResource.HEIGHT, Long.toString(height));
    }

    private JSONArray makeRequest(Context context) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet((String) context.getAttribute("recipesLink", 3));
        request.addHeader("accept", "application/json");
        HttpResponse response = client.execute(request);
        String json = IOUtils.toString(response.getEntity().getContent());
        return new JSONArray(json);
    }
}